package org.apache.parquet.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.api.WriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

/**
 * An abstract builder class for ParquetWriter instances.
 *
 * Object models should extend this builder to provide writer configuration
 * options.
 *
 * This class is copied from parquet-mr.
 *
 * @param <T> The type of objects written by the constructed ParquetWriter.
 * @param <SELF> The type of this builder that is returned by builder methods
 */
public abstract class ParquetWriterBuilder<T, SELF extends ParquetWriterBuilder<T, SELF>> {
  public static final int DEFAULT_BLOCK_SIZE = 128 * 1024 * 1024;
  public static final int DEFAULT_PAGE_SIZE = 1024 * 1024;
  public static final CompressionCodecName DEFAULT_COMPRESSION_CODEC_NAME =
      CompressionCodecName.UNCOMPRESSED;
  public static final boolean DEFAULT_IS_DICTIONARY_ENABLED = true;
  public static final boolean DEFAULT_IS_VALIDATING_ENABLED = false;
  public static final ParquetProperties.WriterVersion DEFAULT_WRITER_VERSION =
      ParquetProperties.WriterVersion.PARQUET_1_0;

  private final Path file;
  private Configuration conf = new Configuration();
  private ParquetFileWriter.Mode mode;
  private CompressionCodecName codecName = DEFAULT_COMPRESSION_CODEC_NAME;
  private int rowGroupSize = DEFAULT_BLOCK_SIZE;
  private int pageSize = DEFAULT_PAGE_SIZE;
  private int dictionaryPageSize = DEFAULT_PAGE_SIZE;
  private boolean enableDictionary = DEFAULT_IS_DICTIONARY_ENABLED;
  private boolean enableValidation = DEFAULT_IS_VALIDATING_ENABLED;
  private ParquetProperties.WriterVersion writerVersion = DEFAULT_WRITER_VERSION;

  protected ParquetWriterBuilder(Path file) {
    this.file = file;
  }

  /**
   * @return this as the correct subclass of ParquetWriter.Builder.
   */
  protected abstract SELF self();

  /**
   * @return an appropriate WriteSupport for the object model.
   */
  protected abstract WriteSupport<T> getWriteSupport(Configuration conf);

  /**
   * Set the {@link Configuration} used by the constructed writer.
   *
   * @param conf a {@code Configuration}
   * @return this builder for method chaining.
   */
  public SELF withConf(Configuration conf) {
    this.conf = conf;
    return self();
  }

  /**
   * Set the {@link ParquetFileWriter.Mode write mode} used when creating the
   * backing file for this writer.
   *
   * @param mode a {@code ParquetFileWriter.Mode}
   * @return this builder for method chaining.
   */
  public SELF withWriteMode(ParquetFileWriter.Mode mode) {
    this.mode = mode;
    return self();
  }

  /**
   * Set the {@link CompressionCodecName compression codec} used by the
   * constructed writer.
   *
   * @param codecName a {@code CompressionCodecName}
   * @return this builder for method chaining.
   */
  public SELF withCompressionCodec(CompressionCodecName codecName) {
    this.codecName = codecName;
    return self();
  }

  /**
   * Set the Parquet format row group size used by the constructed writer.
   *
   * @param rowGroupSize an integer size in bytes
   * @return this builder for method chaining.
   */
  public SELF withRowGroupSize(int rowGroupSize) {
    this.rowGroupSize = rowGroupSize;
    return self();
  }

  /**
   * Set the Parquet format page size used by the constructed writer.
   *
   * @param pageSize an integer size in bytes
   * @return this builder for method chaining.
   */
  public SELF withPageSize(int pageSize) {
    this.pageSize = pageSize;
    return self();
  }

  /**
   * Set the Parquet format dictionary page size used by the constructed
   * writer.
   *
   * @param dictionaryPageSize an integer size in bytes
   * @return this builder for method chaining.
   */
  public SELF withDictionaryPageSize(int dictionaryPageSize) {
    this.dictionaryPageSize = dictionaryPageSize;
    return self();
  }

  /**
   * Enables dictionary encoding for the constructed writer.
   *
   * @return this builder for method chaining.
   */
  public SELF enableDictionaryEncoding() {
    this.enableDictionary = true;
    return self();
  }

  /**
   * Enable or disable dictionary encoding for the constructed writer.
   *
   * @param enableDictionary whether dictionary encoding should be enabled
   * @return this builder for method chaining.
   */
  public SELF withDictionaryEncoding(boolean enableDictionary) {
    this.enableDictionary = enableDictionary;
    return self();
  }

  /**
   * Enables validation for the constructed writer.
   *
   * @return this builder for method chaining.
   */
  public SELF enableValidation() {
    this.enableValidation = true;
    return self();
  }

  /**
   * Enable or disable validation for the constructed writer.
   *
   * @param enableValidation whether validation should be enabled
   * @return this builder for method chaining.
   */
  public SELF withValidation(boolean enableValidation) {
    this.enableValidation = enableValidation;
    return self();
  }

  /**
   * Set the {@link ParquetProperties.WriterVersion format version} used by the constructed
   * writer.
   *
   * @param version a {@code WriterVersion}
   * @return this builder for method chaining.
   */
  public SELF withWriterVersion(ParquetProperties.WriterVersion version) {
    this.writerVersion = version;
    return self();
  }

  /**
   * Build a {@link ParquetWriter} with the accumulated configuration.
   *
   * @return a configured {@code ParquetWriter} instance.
   * @throws IOException
   */
  public ParquetWriter<T> build() throws IOException {
    return new ParquetWriter<T>(file, mode, getWriteSupport(conf), codecName,
        rowGroupSize, pageSize, dictionaryPageSize, enableDictionary,
        enableValidation, writerVersion, conf);
  }
}
