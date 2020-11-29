package resources

import io.circe.generic.JsonCodec

@JsonCodec final case class PropertyOwner(owner: UserData)

@JsonCodec final case class UserData(
                                      ID: String,
                                      user_login: String,
                                      user_pass: String,
                                      user_nicename: String,
                                      user_email: String,
                                      user_url: String,
                                      user_registered: String,
                                      user_activation_key: String,
                                      user_status: String,
                                      display_name: String,
                                      user_meta: Map[String, List[String]],
                                      properties: Map[String, PropertyRecord]
                                    )

@JsonCodec final case class PropertyRecord(
                                            property_data: PropertyData,
                                            comment: Option[Map[String, Map[String, Option[String]]]],
                                            comment_meta: Option[Map[String, CommentMetaData]],
                                            calendar_data: Option[List[Map[String, Option[String]]]],
                                            conversation_data: Map[String, ConversationData]
                                          )

@JsonCodec final case class PropertyData(
                                          property_id: Int,
                                          property_fields: PropertyFields
                                        )

@JsonCodec final case class PropertyFields(post_title: List[String]) //TODO: Include all fields.
@JsonCodec final case class CommentMetaData(
                                             city: Option[List[String]],
                                             country: Option[List[String]],
                                             rating: Option[List[String]],
                                             ticket_id: Option[List[String]]
                                           )

@JsonCodec final case class ConversationData(
                                              id: Option[String],
                                              operator_id: Option[String],
                                              user_name: Option[String],
                                              ticket_id: Option[String],
                                              email: Option[String],
                                              status_id: Option[String],
                                              dt: Option[String],
                                              ip: Option[String],
                                              invoice_status: Option[String],
                                              locked_by: Option[String],
                                              post_id: Option[String],
                                              user_surname: Option[String],
                                              accepted: Option[String],
                                              reserved: Option[String],
                                              booked: Option[String],
                                              booking_time: Option[String],
                                              reserveData: Option[String],
                                              hidden: Option[String],
                                              confirmed: Option[String],
                                              reminder: Option[String],
                                              read: Option[String],
                                              last_message: Option[String],
                                              follow_up: Option[String],
                                              client_2_reminder: Option[String],
                                              client_id: Option[String],
                                              arrival_date: Option[String],
                                              internal_notes: Option[String],
                                              category: Option[String],
                                              license: Option[String],
                                              archived: Option[String],
                                              to_check: Option[String],
                                              owner_read: Option[String],
                                              messages: Map[String, Map[String, Option[String]]]
                                            )

