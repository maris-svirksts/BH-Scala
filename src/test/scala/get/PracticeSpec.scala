package get

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Practice._
import resources._

class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {

  val ownerInfoParsed: Stream[IO, Json]           = fetchOwnerInfoParsed()
  val ownerInfoDecoded: Stream[IO, PropertyOwner] = fetchOwnerInfoDecoded()

  val boardParsed: List[Json]           = ownerInfoParsed.compile.toList.unsafeRunSync()
  val boardDecoded: List[PropertyOwner] = ownerInfoDecoded.compile.toList.unsafeRunSync()

  "Owner ID, parsed, first json file" in {
    val ID = boardParsed.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("44731")
  }

  "Owner ID, parsed, second json file" in {
    val ID = boardParsed.tail.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("27524")
  }

  "Owner ID, decoded, first json file" in {
    val ID = boardDecoded.headOption.get.owner.ID
    ID must be("44731")
  }

  "User Meta / nickname" in {
    val nickname = boardParsed.headOption.get.hcursor.downField("owner").downField("user_meta").downField("nickname").as[List[String]].right.value
    nickname.headOption.getOrElse("none") must be("1925cabin")
  }

  "Property ID" in {
    val properties = boardParsed.headOption.get.hcursor.downField("owner").downField("properties").downField("427541")
      .downField("property_data").downField("property_id").as[Int].right.value
    properties must be(427541)
  }

  "No Filter (2 owners)" in {
    boardParsed.size must be(2)
  }

  "Filter (1 owner)" in {
    val filtered = boardParsed.filter( x => x.hcursor.downField("owner").downField("ID").as[String].right.value == "44731")
    filtered.size must be(1)
  }

  "Filter to File, decoded, (1 owner)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID == "44731" } )

    val lines: Seq[String] = for {
      i <- filtered
    } yield i.owner.ID + ", " + i.owner.display_name

    writeFile("test.csv", lines: Seq[String])

    filtered.size must be(1)
  }
}
