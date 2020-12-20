package json.resources

import cats.effect.IO

object HelperFunctions {
  /**
   * @param data      Data to transform in List[String] format.
   * @param separator defines what kind of data it will be: ", " - CSV, "\t" - TSV.
   * @return string of data that's ready to be saved.
   */
  def convertToFileFormat(data: List[String], separator: String): IO[String] = IO(data.mkString(separator))

  /**
   * @param value ADT element that corresponds to the type defined below.
   * @return ADT element value.
   *
   *         Shortcode and error protection for filter data fields.
   */
  def getValue(value: Option[List[Option[String]]]): String = {
    value.flatMap(x => x.headOption.flatten).getOrElse("")
  }

  /**
   * @return base path to owner files.
   */
  def systemPath(): String = {
    "I:/"
  }
}
