namespace java com.databricks.parquet.thrift

struct ThriftIntArray {
  1: required list<i32> f;
}

struct ThriftArrayOfIntArray {
  1: required list<list<i32>> f;
}
