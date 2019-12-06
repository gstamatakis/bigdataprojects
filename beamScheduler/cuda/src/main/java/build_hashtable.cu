extern "C" __global__ void build_hashtable(int *R, int R_size, int *hash_table) {
  int offset = blockIdx.x * blockDim.x + threadIdx.x;

  int key = R[offset];
  int hash = key & (R_size-1);

  if (offset < R_size) {
    hash_table[hash] = key;
  }
}