extern "C" __global__ void probe_hashtable(int *S, int S_size, int *hash_table, int ht_size,int* OUT) {//OUT and S have the same size
  int offset = blockIdx.x * blockDim.x + threadIdx.x;
  int stride = blockDim.x * gridDim.x;

  for (int i = offset; i < S_size && i < ht_size; i += stride) {
    int key = S[i];
    int hash = key & (ht_size - 1);

    if (key == hash_table[hash]) {
		OUT[i] = key;
    }else {
        OUT[i] = -1;
    }
  }
}