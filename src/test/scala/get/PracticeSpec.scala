package get

import cats.effect.IO
import fs2.Stream
import get.Practice._
import get.resources._
import io.circe.Json
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

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

    writeFile("src/main/results/test_p1.csv", lines: Seq[String])

    filtered.size must be(1)
  }

  "Filter to File, decoded, (1581 owners)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID.toInt > 500 } )

    val lines = for {
      i <- filtered
      result = List(i.owner.ID, i.owner.user_email, i.owner.display_name)
    } yield result

    val json = ExportJson(lines).asJson.toString()

    writeFile("src/main/results/results.json", Seq(json))

    filtered.size must be(1581)
  }

  "Filter, decoded, (1 property), find Owner for property." in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists(y => {
        y match {
          case (_, value) if (value.property_data.property_id == 31171) => true
          case _ => false
        }
      })
    } )

    val lines: Seq[String] = for {
      i      <- filtered
      (_, v) <- i.owner.properties.getOrElse(Nil)

      if v.property_data.property_id == 31171
    } yield i.owner.ID + ", " + i.owner.display_name + ", " + v.property_data.property_fields.post_title

    writeFile("src/main/results/test_p2.csv", lines: Seq[String])

    filtered.size must be(1)
  }

  "Filter, decoded, (483 properties), published_per_night" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists(y => {
        y match {
          case (_, value) if (
            value.property_data.property_fields.published_per_night.getOrElse(List()).headOption.getOrElse(Some("0")).getOrElse("0").toIntOption.getOrElse(0) >= 300 && value.property_data.property_fields.published_per_night.getOrElse(List()).headOption.getOrElse(Some("0")).getOrElse("0").toIntOption.getOrElse(0) <= 800
            ) => true
          case _ => false
        }
      })
    } )

    val lines: Seq[String] = for {
      i      <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      if value.property_data.property_fields.published_per_night.getOrElse(List()).headOption.getOrElse(Some("0")).getOrElse("0").toIntOption.getOrElse(0) >= 300 && value.property_data.property_fields.published_per_night.getOrElse(List()).headOption.getOrElse(Some("0")).getOrElse("0").toIntOption.getOrElse(0) <= 800
    } yield i.owner.ID + "\t" + i.owner.display_name + "\t" + value.property_data.property_fields.post_title.getOrElse(Nil).headOption.getOrElse(Some("")).getOrElse("none") + "\t" + value.property_data.property_fields.published_per_night.getOrElse(List()).headOption.getOrElse(Some("0")).getOrElse("0")

    writeFile("src/main/results/test_p3.tsv", lines: Seq[String])

    filtered.size must be(483)
  }
}