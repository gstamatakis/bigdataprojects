import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static jcuda.driver.JCudaDriver.*;

public class JCudaImpl {

    public static void main(String args[]) throws IOException {
        setExceptionsEnabled(true);
        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);
        CUmodule build_module = new CUmodule();

        // Build phase
        System.out.println("build phase start");
        String ptxBuild = preparePtxFile("build_hashtable.cu");
        cuModuleLoad(build_module, ptxBuild);

        //Store all the rideIDs for the semi join.
        List<Integer> fares_ids = new ArrayList<>();
        // File to read in HDFS

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create("hdfs://localhost:9000"), conf);

        try (FSDataInputStream in = fs.open(new Path("hdfs://localhost:9000/TaxiFares")); Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                try {
                    String[] tokens = scanner.nextLine().split(",");
                    String last_char = tokens[0].substring(tokens[0].length() - 1);
                    if (last_char.equals("0") || last_char.equals("1") || last_char.equals("2")) {
                        fares_ids.add(Integer.parseInt(tokens[0]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finished reading Taxi Fares");
        }


        //Build a hashtable for HDFS data.
        int[] host_fids = fares_ids.stream().mapToInt(i -> i).toArray(); //Used for semi join. Fare IDs.
        if (host_fids.length == 0) {
            System.out.println("Empty relation read from HDFS!");
            return;
        }
        CUdeviceptr hdfs_device_input = new CUdeviceptr();
        cuMemAlloc(hdfs_device_input, host_fids.length * Sizeof.INT);
        cuMemcpyHtoD(hdfs_device_input, Pointer.to(host_fids), host_fids.length * Sizeof.INT);

        //Will also be used in the probe phase.
        CUdeviceptr hashTableDevice = new CUdeviceptr();
        cuMemAlloc(hashTableDevice, host_fids.length * Sizeof.INT);

        Pointer kernelParameters = Pointer.to(
                Pointer.to(hdfs_device_input),
                Pointer.to(new int[]{host_fids.length}),
                Pointer.to(hashTableDevice)    //Same length as the rides relation.
        );

        // Obtain a function pointer to the build hash table function.
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, build_module, "build_hashtable");

        final int blockSizeX = 256;
        int gridSizeX = (int) Math.ceil((double) host_fids.length / blockSizeX);
        cuLaunchKernel(function,
                gridSizeX, 1, 1,      // Grid dimension
                blockSizeX, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();

        //Probe phase
        System.out.println("probe phase start");
        String ptxProbe = preparePtxFile("probe_hashtable.cu");
        CUmodule probe_module = new CUmodule();
        cuModuleLoad(probe_module, ptxProbe);

        Properties props = new Properties();
        props.put("bootstrap.servers", "rserver05:6667,rserver06:6667,rserver07:6667");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "spark");//change accordingly
        KafkaConsumer<String, String> data_consumer = new KafkaConsumer<>(props);
        data_consumer.subscribe(Collections.singletonList("g5"));

        Properties props2 = new Properties();
        props2.put("bootstrap.servers", "rserver05:6667,rserver06:6667,rserver07:6667");
        props2.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props2.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> data_producer = new KafkaProducer<>(props2);

        //Allocate memory for streamed data.
        int probe_relation_length = 250000;
        int[] host_rids = new int[probe_relation_length];   //Store the ride IDs here
        CUdeviceptr kafka_device_input = new CUdeviceptr();
        cuMemAlloc(kafka_device_input, probe_relation_length * Sizeof.INT);

        // Allocate device output memory. It's a semi-join therefore same as kafka_input
        CUdeviceptr joinOutput = new CUdeviceptr();
        cuMemAlloc(joinOutput, probe_relation_length * Sizeof.INT);

        //Change the kernel function
        cuModuleGetFunction(function, probe_module, "probe_hashtable");
        Pointer kernelParameters2 = Pointer.to(
                Pointer.to(kafka_device_input),
                Pointer.to(new int[]{host_rids.length}),
                Pointer.to(hashTableDevice),
                Pointer.to(new int[]{host_fids.length}),
                Pointer.to(joinOutput)    //Same length as the rides relation.

        );

        try {
            while (true) {
                HashMap<Integer, String> rides = new HashMap<>();//<id,string>, use to recover the ride values after join
                int kafka_tuples = 0;
                for (ConsumerRecord<String, String> record : data_consumer.poll(1000)) {
                    String ride = record.value();
                    int rideid = Integer.parseInt(ride.split(",")[0]);
                    rides.put(rideid, ride);
                    host_rids[kafka_tuples++] = rideid;
                }
                if (rides.isEmpty()) {
                    continue;
                }

                //Copy to device
                cuMemcpyHtoD(kafka_device_input, Pointer.to(host_rids), probe_relation_length * Sizeof.INT);

                // Call the kernel function.
                final int blockSizeX_2 = 256;
                int gridSizeX_2 = (int) Math.ceil((double) host_rids.length / blockSizeX);
                cuLaunchKernel(function,
                        gridSizeX_2, 1, 1,      // Grid dimension
                        blockSizeX_2, 1, 1,      // Block dimension
                        0, null,               // Shared memory size and stream
                        kernelParameters2, null // Kernel- and extra parameters
                );
                cuCtxSynchronize();

                int[] hostOutput = new int[host_rids.length];//The rideIDs that survived. Output
                cuMemcpyDtoH(Pointer.to(hostOutput), joinOutput, host_rids.length * Sizeof.INT);

                for (int rid : hostOutput) {
                    if (rid != -1) {
                        continue;
                    }
                    ProducerRecord<String, String> record = new ProducerRecord<>("g6", null, rides.get(rid));
                    data_producer.send(record);
                }
                System.out.println("Msg/sec: " + kafka_tuples);
            }
        } catch (Exception e) {
            cuMemFree(hdfs_device_input);
            cuMemFree(kafka_device_input);
            cuMemFree(joinOutput);
            e.printStackTrace();
        }
    }


    /**
     * The extension of the given file name is replaced with "ptx".
     * If the file with the resulting name does not exist, it is
     * compiled from the given file using NVCC. The name of the
     * PTX file is returned.
     *
     * @param cuFileName The name of the .CU file
     * @return The name of the PTX file
     * @throws IOException If an I/O error occurs
     */
    private static String preparePtxFile(String cuFileName) throws IOException {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1) {
            endIndex = cuFileName.length() - 1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";
        File ptxFile = new File(ptxFileName);
        if (ptxFile.exists()) {
            return ptxFileName;
        }

        File cuFile = new File(cuFileName);
        if (!cuFile.exists()) {
            throw new IOException("Input file not found: " + cuFileName);
        }
        String modelString = "-m" + System.getProperty("sun.arch.data.model");
        String arch = " -gencode arch=compute_35,code=sm_35 -rdc=true";
        String command = "nvcc " + modelString + arch + " -ptx " + cuFile.getPath() + " -o " + ptxFileName;

        System.out.println("Executing\n" + command);
        Process process = Runtime.getRuntime().exec(command);

        String errorMessage = new String(toByteArray(process.getErrorStream()));
        String outputMessage = new String(toByteArray(process.getInputStream()));
        int exitValue = 0;
        try {
            exitValue = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for nvcc output", e);
        }

        if (exitValue != 0) {
            System.out.println("nvcc process exitValue " + exitValue);
            System.out.println("errorMessage:\n" + errorMessage);
            System.out.println("outputMessage:\n" + outputMessage);
            throw new IOException(
                    "Could not create .ptx file: " + errorMessage);
        }

        System.out.println("Finished creating PTX file");
        return ptxFileName;
    }

    /**
     * Fully reads the given InputStream and returns it as a byte array
     *
     * @param inputStream The input stream to read
     * @return The byte array containing the data from the input stream
     * @throws IOException If an I/O error occurs
     */
    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }


}