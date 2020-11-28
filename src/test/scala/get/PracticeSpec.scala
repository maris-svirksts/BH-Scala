package get

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Practice._

class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {

  val ownerInfoOrError: Stream[IO, Json] = fetchOwnerInfoParsed()
  val board: List[Json] = ownerInfoOrError.compile.toList.unsafeRunSync()

  "Owner ID, first json file" in {
    val ID = board.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("44731")
  }

  "Owner ID, second json file" in {
    val ID = board.tail.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("27524")
  }

  "User Meta / nickname" in {
    val nickname = board.headOption.get.hcursor.downField("owner").downField("user_meta").downField("nickname").as[List[String]].right.value
    nickname.headOption.getOrElse("none") must be("1925cabin")
  }

  "Property ID" in {
    val properties = board.headOption.get.hcursor.downField("owner").downField("properties").downField("427541")
      .downField("property_data").downField("property_id").as[Int].right.value
    properties must be(427541)
  }

  "No Filter (2 owners)" in {
    board.size must be(2)
  }

  "Filter (1 owner)" in {
    val filtered = board.filter( x => x.hcursor.downField("owner").downField("ID").as[String].right.value == "44731")
    filtered.size must be(1)
  }
}
