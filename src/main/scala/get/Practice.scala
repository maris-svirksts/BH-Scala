package get

import cats.effect.IO

import io.circe.Json
import io.circe.generic.JsonCodec
import io.circe.generic.extras._
import io.circe.fs2._

import fs2.Stream
import scalaj.http.Http

/*
     Bouncing ideas:

     user_meta:           Map[String, RecordList],
     properties:          Map[String, PropertyRecord]

     PropertyRecord is a case class, RecordList is a List[String] - probably a bad idea, might need to decide on one
     because the calls to get variables will differ. Or, have case classes for higher levels only. Or, move to Optics?
 */

object Practice {
  implicit val config: Configuration = Configuration.default

  type RecordList = List[String]
  type Key        = String

  @JsonCodec final case class PropertyOwner(owner: UserData)
  @JsonCodec final case class UserData(
                                        ID:                  String,
                                        user_login:          String,
                                        user_pass:           String,
                                        user_nicename:       String,
                                        user_email:          String,
                                        user_url:            String,
                                        user_registered:     String,
                                        user_activation_key: String,
                                        user_status:         String,
                                        display_name:        String,
                                        user_meta:           Map[String, RecordList],
                                        properties:          Map[String, PropertyRecord]
                                      )

  @JsonCodec final case class PropertyRecord(
                                              property_data:     PropertyData,
                                              comment:           Map[Key, Map[Key, String]],
                                              comment_meta:      Map[Key, CommentMetaData],
                                              calendar_data:     List[Map[Key, Option[String]]],
                                              conversation_data: Map[Key, ConversationData]
                                            )
  @JsonCodec final case class PropertyData(
                                            property_id: Int,
                                            property_fields: PropertyFields
                                          )
  @JsonCodec final case class PropertyFields(post_title: RecordList) //TODO: Include all fields.
  @JsonCodec final case class CommentMetaData(
                                               city:      RecordList,
                                               country:   RecordList,
                                               rating:    RecordList,
                                               ticket_id: RecordList
                                             )
  @JsonCodec final case class ConversationData(
                                                id:                Option[String],
                                                operator_id:       Option[String],
                                                user_name:         Option[String],
                                                ticket_id:         Option[String],
                                                email:             Option[String],
                                                status_id:         Option[String],
                                                dt:                Option[String],
                                                ip:                Option[String],
                                                invoice_status:    Option[String],
                                                locked_by:         Option[String],
                                                post_id:           Option[String],
                                                user_surname:      Option[String],
                                                accepted:          Option[String],
                                                reserved:          Option[String],
                                                booked:            Option[String],
                                                booking_time:      Option[String],
                                                reserveData:       Option[String],
                                                hidden:            Option[String],
                                                confirmed:         Option[String],
                                                reminder:          Option[String],
                                                read:              Option[String],
                                                last_message:      Option[String],
                                                follow_up:         Option[String],
                                                client_2_reminder: Option[String],
                                                client_id:         Option[String],
                                                arrival_date:      Option[String],
                                                internal_notes:    Option[String],
                                                category:          Option[String],
                                                license:           Option[String],
                                                archived:          Option[String],
                                                to_check:          Option[String],
                                                owner_read:        Option[String],
                                                messages:          Map[Key, Map[Key, Option[String]]]
                                              )

  def fetchOwnerInfo(): Stream[IO, Json] = {
    val firstOwner: String  = Http("https://www.boutique-homes.com/remote_search/data.json").asString.body
    val secondOwner: String = Http("https://www.boutique-homes.com/remote_search/data-p2.json").asString.body

    val merged: String = "[" + firstOwner + ", " + secondOwner + "]" // TODO: crude, need to improve.

    val stringStream: Stream[IO, String] = Stream(merged)

    stringStream.through(stringArrayParser)
  }
}