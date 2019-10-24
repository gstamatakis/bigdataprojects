import org.apache.storm.metric.api.CountMetric;
import org.apache.storm.shade.org.joda.time.DateTime;
import org.apache.storm.shade.org.joda.time.DateTimeZone;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

import java.util.*;
import java.util.logging.Logger;

public class LookupBolt extends BaseRichBolt {
    int width;                //w in CM , m in Bloom
    BitSet BloomBitArray;     //For Bloom Filter lookups
    int[] CountMinCounters;   //For Count Min sketches
    int[] seeds;              //Used for hashing
    int queryType;            //0: Bloom    1: CountMin

    private long msg_count;
    Logger log;
    OutputCollector collector;
    int queriesProcessed;
    int insertionsProcessed;
    transient CountMetric countMetric;
    private Map stormConf;
    private Timer timer;
    private final int TIMER_PERIOD = 20;
    private boolean finished;

    public LookupBolt(int width, int numOfHashFuncs) {
        this.width = width;
        Random random = new Random();
        seeds = new int[numOfHashFuncs];
        for (int i = 0; i < numOfHashFuncs; i++) {
            seeds[i] = random.nextInt();
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.log = Logger.getLogger(LookupBolt.class.getName());
        this.queryType = ((Long) stormConf.get("QUERY_TYPE")).intValue();
        this.msg_count = 0;
        this.queriesProcessed = 0;
        this.insertionsProcessed = 0;
        this.stormConf = stormConf;
        this.finished = false;

        switch (queryType) {
            case 0:
                this.BloomBitArray = new BitSet(width);
                break;
            case 1:
                this.CountMinCounters = new int[width];
                break;
            default:
                log.severe("Default case in LookupBolt prepare!!");
        }
        this.countMetric = new CountMetric();
        context.registerMetric("execute_count", countMetric, 30);

        //Timer will fire every 20s
        DateTime now = new DateTime().withZone(DateTimeZone.UTC);
        int roundSeconds = ((now.getSecondOfMinute() / TIMER_PERIOD) * TIMER_PERIOD) + TIMER_PERIOD;
        DateTime startAt = now.minuteOfHour().roundFloorCopy().plusSeconds(roundSeconds);
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new PeriodicTask(), startAt.toDate(), TIMER_PERIOD * 1000L);
    }

    @Override
    public void execute(Tuple input) {
        if (finished) {
            Utils.sleep(10000);
            log.info("Awaiting shutdown...");
            System.out.println("Finished=true!");
            return;
        }

        String data = input.getStringByField("data");
        switch (input.getSourceComponent()) {
            case "dataSpout":  //Insertion
                for (int hashSeed : seeds) {
                    int pos = MurmurHash(data, 0, data.length(), hashSeed, width);
                    switch (queryType) {
                        case 0:
                            BloomBitArray.set(pos, true);
                            break;
                        case 1:
                            CountMinCounters[pos]++;
                            break;
                        default:
                            log.severe("Default case in LookupBolt insertion!!");
                            collector.fail(input);
                            return;
                    }
                }
                insertionsProcessed++;
                break;
            case "querySpout":    // Query received
                int qid = input.getIntegerByField("queryID");
                int queryResult = Integer.MAX_VALUE;
                for (int i = 0; i < seeds.length && queryResult != 0; i++) {
                    int hashSeed = seeds[i];
                    int pos = MurmurHash(data, 0, data.length(), hashSeed, width);
                    switch (queryType) {
                        case 0:
                            queryResult = Math.min(queryResult, BloomBitArray.get(pos) ? 1 : 0);
                            break;
                        case 1:
                            queryResult = Math.min(queryResult, CountMinCounters[pos]);
                            break;
                        default:
                            log.severe("Default case in LookupBolt lookup!!");
                            collector.fail(input);
                            return;
                    }
                }

                collector.emit(new Values(qid, queryResult));
                queriesProcessed++;
                break;
            default:
                log.severe("Default case in LookupBolt input.getSourceComponent()!!\n" + input.toString());
                collector.fail(input);
                return;
        }

        msg_count++;
        collector.ack(input);
        countMetric.incr();
    }

    /**
     * Periodic task that will attempt to kill the topology if the number of processed tuples hasn't changed since the
     * last execution.
     */
    class PeriodicTask extends TimerTask {
        private long msg_cnt;

        public PeriodicTask() {
            this.msg_cnt = -1;
        }

        @Override
        public void run() {
            if (this.msg_cnt == msg_count) {
                timer.cancel();
                log.info("Timer canceled.");
                try {
                    log.info("Trying to shutdown because the bolt hasn't received any tuples for at least " + TIMER_PERIOD + " sec.");
                    NimbusClient.getConfiguredClient(stormConf).getClient().killTopology("MyTopology"); //VERY UNRELIABLE
                    log.info("NimbusClient successfully shutdown!");
                } catch (Exception e) {
                    log.severe("Failed to kill topology via NimbusClient: \n" + e.toString());
                } finally {
                    finished = true;
                }
            } else {
                this.msg_cnt = msg_count;
                log.info("Read: " + msg_count + " messages in this bolt.\n" +
                        "Insertions processed: " + insertionsProcessed + "\n" +
                        "Queries processed: " + queriesProcessed);

            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("queryID", "decision"));
    }

    /**
     * Returns the MurmurHash3_x86_32 hash of the UTF-8 bytes of the String without actually encoding
     * the string to a temporary buffer.  This is more than 2x faster than hashing the result
     * of String.getBytes().
     * <p>
     * Has been tweaked to only return positive values within specified bound {@link LookupBolt#width}.
     */
    private int MurmurHash(CharSequence data, int offset, int len, int seed, int width) {
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int h1 = seed;

        int pos = offset;
        int end = offset + len;
        int k1 = 0;
        int k2 = 0;
        int shift = 0;
        int bits = 0;
        int nBytes = 0;   // length in UTF8 bytes


        while (pos < end) {
            int code = data.charAt(pos++);
            if (code < 0x80) {
                k2 = code;
                bits = 8;
            } else if (code < 0x800) {
                k2 = (0xC0 | (code >> 6))
                        | ((0x80 | (code & 0x3F)) << 8);
                bits = 16;
            } else if (code < 0xD800 || code > 0xDFFF || pos >= end) {
                // we check for pos>=end to encode an unpaired surrogate as 3 bytes.
                k2 = (0xE0 | (code >> 12))
                        | ((0x80 | ((code >> 6) & 0x3F)) << 8)
                        | ((0x80 | (code & 0x3F)) << 16);
                bits = 24;
            } else {
                // surrogate pair
                // int utf32 = pos < end ? (int) data.charAt(pos++) : 0;
                int utf32 = (int) data.charAt(pos++);
                utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
                k2 = (0xff & (0xF0 | (utf32 >> 18)))
                        | ((0x80 | ((utf32 >> 12) & 0x3F))) << 8
                        | ((0x80 | ((utf32 >> 6) & 0x3F))) << 16
                        | (0x80 | (utf32 & 0x3F)) << 24;
                bits = 32;
            }


            k1 |= k2 << shift;

            // int used_bits = 32 - shift;  // how many bits of k2 were used in k1.
            // int unused_bits = bits - used_bits; //  (bits-(32-shift)) == bits+shift-32  == bits-newshift

            shift += bits;
            if (shift >= 32) {
                // mix after we have a complete word

                k1 *= c1;
                k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
                k1 *= c2;

                h1 ^= k1;
                h1 = (h1 << 13) | (h1 >>> 19);  // ROTL32(h1,13);
                h1 = h1 * 5 + 0xe6546b64;

                shift -= 32;
                // unfortunately, java won't let you shift 32 bits off, so we need to check for 0
                if (shift != 0) {
                    k1 = k2 >>> (bits - shift);   // bits used == bits - newshift
                } else {
                    k1 = 0;
                }
                nBytes += 4;
            }

        } // inner

        // handle tail
        if (shift > 0) {
            nBytes += shift >> 3;
            k1 *= c1;
            k1 = (k1 << 15) | (k1 >>> 17);  // ROTL32(k1,15);
            k1 *= c2;
            h1 ^= k1;
        }

        // finalization
        h1 ^= nBytes;

        // fmix(h1);
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        if (h1 < 0) {
            h1 = ~h1;
        }

        return h1 % width;
    }
}
