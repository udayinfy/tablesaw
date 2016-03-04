package com.deathrayresearch.outlier.util;

import com.deathrayresearch.outlier.columns.FloatColumn;
import com.deathrayresearch.outlier.columns.IntColumn;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.util.FastMath;

import java.util.List;

/**
 *
 */
public class StatUtil {

  private StatUtil() {}

  public static float sum(final FloatColumn values) {
    float sum;
    sum = 0.0f;
    while (values.hasNext()) {
      float value = values.next();
      if (value != Float.NaN) {
        sum += value;
      }
    }
    return sum;
  }

  public static float product(final float[] values) {
    float product = 1.0f;
    boolean empty = true;
    for (float value : values) {
      if (value != Float.NaN) {
        empty = false;
        product *= value;
      }
    }
    if (empty) {
      return Float.NaN;
    }
    return product;
  }

  public static float min(final FloatColumn values) {
    if (values.size() == 0) {
      return Float.NaN;
    }
    float min = values.firstElement();
    while (values.hasNext()) {
      float value = values.next();
      if (!Float.isNaN(value)) {
        min = (min < value) ? min : value;
      }
    }
    return min;
  }

  public static float max(final FloatColumn values) {
    if (values.size() == 0) {
      return Float.NaN;
    }
    float max = values.firstElement();
    values.reset();
    while (values.hasNext()) {
      float value = values.next();
      if (!Float.isNaN(value)) {
        if (value > max) {
          max = value;
        }
      }
    }
    return max;
  }

  /**
   * Returns the (sample) variance of the available values.
   *
   * <p>This method returns the bias-corrected sample variance (using {@code n - 1} in
   * the denominator).
   *
   * @return The variance, Double.NaN if no values have been added
   * or 0.0 for a single value set.
   */
  public static float variance(FloatColumn column) {
    float avg = mean(column);
    column.reset();
    float sumSquaredDiffs = 0.0f;
    while (column.hasNext()) {
      float next = column.next();
      float diff = next - avg;
      float sqrdDiff = diff * diff;
      sumSquaredDiffs += sqrdDiff;
    }
    //float sumMinusAverage = sum(column) - mean(column) * column.size();
    return sumSquaredDiffs / (column.size() - 1);
    //return (sumMinusAverage * sumMinusAverage) / (column.size() - 1);
  }

  /**
   * Returns the standard deviation of the available values.
   *
   * @return The standard deviation, Double.NaN if no values have been added
   * or 0.0 for a single value set.
   */
  public static float standardDeviation(FloatColumn values) {
    float stdDev = Float.NaN;
    int N = values.size();
    if (N > 0) {
      if (N > 1) {
        stdDev = (float) FastMath.sqrt(variance(values));
      } else {
        stdDev = 0.0f;
      }
    }
    return stdDev;
  }


  public static float mean(FloatColumn values) {
    return values.sum() / (float) values.size();
  }

  public static Stats stats(final FloatColumn values) {
    Stats stats = new Stats();
    stats.min = min(values);
    stats.max = max(values);
    stats.n = values.size();
    stats.mean = values.sum() / (float) stats.n;
    stats.variance = variance(values);
    return stats;
  }

  public static Stats stats(final IntColumn ints) {
    FloatColumn values = FloatColumn.create(ints.name(), ints.toFloatArray());
    Stats stats = new Stats();
    stats.min = min(values);
    stats.max = max(values);
    stats.n = values.size();
    stats.mean = values.sum() / (float) stats.n;
    stats.variance = variance(values);
    return stats;
  }


  /**
   * Returns the sample mode(s).  The mode is the most frequently occurring
   * value in the sample. If there is a unique value with maximum frequency,
   * this value is returned as the only element of the output array. Otherwise,
   * the returned array contains the maximum frequency elements in increasing
   * order.  For example, if {@code sample} is {0, 12, 5, 6, 0, 13, 5, 17},
   * the returned array will have length two, with 0 in the first element and
   * 5 in the second.
   *
   * <p>NaN values are ignored when computing the mode - i.e., NaNs will never
   * appear in the output array.  If the sample includes only NaNs or has
   * length 0, an empty array is returned.</p>
   *
   * @param sample input data
   * @return array of array of the most frequently occurring element(s) sorted in ascending order.
   * @throws MathIllegalArgumentException if the indices are invalid or the array is null
   * @since 3.3
   */
  public static float[] mode(float[] sample) throws MathIllegalArgumentException {
    if (sample == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }
    return getMode(sample, 0, sample.length);
  }

  /**
   * Returns the sample mode(s).  The mode is the most frequently occurring
   * value in the sample. If there is a unique value with maximum frequency,
   * this value is returned as the only element of the output array. Otherwise,
   * the returned array contains the maximum frequency elements in increasing
   * order.  For example, if {@code sample} is {0, 12, 5, 6, 0, 13, 5, 17},
   * the returned array will have length two, with 0 in the first element and
   * 5 in the second.
   *
   * <p>NaN values are ignored when computing the mode - i.e., NaNs will never
   * appear in the output array.  If the sample includes only NaNs or has
   * length 0, an empty array is returned.</p>
   *
   * @param sample input data
   * @param begin index (0-based) of the first array element to include
   * @param length the number of elements to include
   *
   * @return array of array of the most frequently occurring element(s) sorted in ascending order.
   * @throws MathIllegalArgumentException if the indices are invalid or the array is null
   * @since 3.3
   */
  public static float[] mode(float[] sample, final int begin, final int length) {
    if (sample == null) {
      throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
    }

    if (begin < 0) {
      throw new NotPositiveException(LocalizedFormats.START_POSITION, Integer.valueOf(begin));
    }

    if (length < 0) {
      throw new NotPositiveException(LocalizedFormats.LENGTH, Integer.valueOf(length));
    }

    return getMode(sample, begin, length);
  }

  /**
   * Private helper method.
   * Assumes parameters have been validated.
   * @param values input data
   * @param begin index (0-based) of the first array element to include
   * @param length the number of elements to include
   * @return array of array of the most frequently occurring element(s) sorted in ascending order.
   */
  private static float[] getMode(float[] values, final int begin, final int length) {
    // Add the values to the frequency table
    Frequency freq = new Frequency();
    for (int i = begin; i < begin + length; i++) {
      final float value = values[i];
      if (!Float.isNaN(value)) {
        freq.addValue(Float.valueOf(value));
      }
    }
    List<Comparable<?>> list = freq.getMode();
    // Convert the list to an array of primitive double
    float[] modes = new float[list.size()];
    int i = 0;
    for(Comparable<?> c : list) {
      modes[i++] = ((Float) c).floatValue();
    }
    return modes;
  }
}
