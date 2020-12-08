package get

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Practice._
import get.resources._

import scala.collection.immutable.{AbstractMap, SeqMap, SortedMap}

class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {

  val ownerInfoParsed: Stream[IO, Json]           = fetchOwnerInfoParsed()
  val ownerInfoDecoded: Stream[IO, PropertyOwner] = fetchOwnerInfoDecoded()

  val boardParsed: List[Json]           = ownerInfoParsed.compile.toList.unsafeRunSync()
  val boardDecoded: List[PropertyOwner] = ownerInfoDecoded.compile.toList.unsafeRunSync()

  "Owner ID, parsed, first json file" in {
    val ID = boardParsed.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("100")
  }

  "Owner ID, parsed, second json file" in {
    val ID = boardParsed.tail.headOption.get.hcursor.downField("owner").downField("ID").as[String].right.value
    ID must be("1002")
  }

  "Owner ID, decoded, first json file" in {
    val ID = boardDecoded.headOption.get.owner.ID
    ID must be("100")
  }

  "User Meta / nickname" in {
    val nickname = boardParsed.headOption.get.hcursor.downField("owner").downField("user_meta").downField("nickname").as[List[String]].right.value
    nickname.headOption.getOrElse("none") must be("Hirai")
  }

  "Property ID" in {
    val properties = boardParsed.tail.headOption.get.hcursor.downField("owner").downField("properties").downField("73611")
      .downField("property_data").downField("property_id").as[Int].right.value
    properties must be(73611)
  }

  "No Filter (2 owners)" in {
    boardParsed.size must be(1881)
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

    writeFile("src/main/scala/get/test.csv", lines: Seq[String])

    filtered.size must be(1)
  }

  "Filter to File, decoded, (x owners)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID.toInt > 500 } )

    val lines = for {
      i <- filtered
      result = List(i.owner.ID, i.owner.user_email, i.owner.display_name)
    } yield result

    val json = ExportJson(lines).asJson.toString()

    writeFile("owners/results.json", Seq(json))

    filtered.size must be(1581)
  }

  "Filter, decoded, (1 property)" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).filter(y => {
        y match {
          case (key, value) if(value.property_data.property_id == 31171) => true
          case _ => false
        }
      }).size > 0
    } )

    /*val filtered_v2 = for {
      i <- boardDecoded
      properties = for{
        y <- i.owner.properties.getOrElse(Nil)
        if(y._2.property_data.property_id == 31171)
      } yield y
    } yield ???*/

    val lines: Seq[String] = for {
      i <- filtered
    } yield i.owner.ID + ", " + i.owner.display_name + ", " + i.owner.properties.get("401776").property_data.property_fields.post_title
    // i.owner.properties.get("401776").property_data.property_fields.post_title should fail catastrophically.

    writeFile("src/main/scala/get/test.csv", lines: Seq[String])

    filtered.size must be(1)
  }
}