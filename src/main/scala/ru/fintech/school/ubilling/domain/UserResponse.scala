package ru.fintech.school.ubilling.domain

import java.util.UUID

import ru.fintech.school.ubilling.domain.BillResponse._
import spray.json.DefaultJsonProtocol._
import spray.json._

case class UserView(email: String, phone: Option[String])

case class UserResponse(userId: Long, view: UserView)

case class UserBillsResponse(userId: Long, bills: Seq[BillView])

case class BillUsersResponse(billId: UUID, users: Seq[Long])

object UserResponse {
  implicit lazy val userViewFormat: RootJsonFormat[UserView] = jsonFormat2(UserView)
  implicit lazy val userResponseFormat: RootJsonFormat[UserResponse] = jsonFormat2(UserResponse.apply)
  implicit lazy val userBillsResponseFormat: RootJsonFormat[UserBillsResponse] = jsonFormat2(UserBillsResponse)
  implicit lazy val billUsersResponseFormat: RootJsonFormat[BillUsersResponse] = jsonFormat2(BillUsersResponse)
}
