package get

import io.circe
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Practice._

class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {

  val ownerInfoOrError: Either[circe.Error, PropertyOwner] = fetchOwnerInfo()
  val board: PropertyOwner = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))

  "Owner ID" in {
    val ID = board.owner.ID

    ID must be("44731")
  }

  "User Meta / nickname" in {
    val nickname = board.owner.user_meta("nickname")

    nickname.headOption.getOrElse("none") must be("1925cabin")
  }

  "Property ID" in {
    val properties = board.owner.properties("427541").property_data.property_id

    properties must be(427541)
  }
}
