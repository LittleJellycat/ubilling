package ru.fintech.school.ubilling

import java.time.LocalDate
import java.util.UUID

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest._
import ru.fintech.school.ubilling.dao.UserBillDAL
import ru.fintech.school.ubilling.dao.UserBillDAL.{createSchema, dropSchema}
import ru.fintech.school.ubilling.domain.{BillResponse, BillView, BillViewItem}
import ru.fintech.school.ubilling.domain.BillResponse._
import ru.fintech.school.ubilling.handler._
import ru.fintech.school.ubilling.routing.AppRouting

import scala.collection.mutable

class AppRoutingSpec extends WordSpec
  with SprayJsonSupport
  with Matchers
  with ScalatestRouteTest {
  dropSchema.onComplete(_ => createSchema)
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val billService = new BillServiceImpl(UserBillDAL)
  val userService = new UserServiceImpl(UserBillDAL)
  val userBillService = new UserBillServiceImpl(UserBillDAL)
  val handler = new RequestHandlerImpl(userService, billService, userBillService)
  val route = AppRouting.route(handler)

  "The service" should {
    "Add new bills" in {
      val billViews = (1 to 5).map(n => BillView(
        s"bill-$n",
        (1 to 10).map(m => BillViewItem(s"order-$n-$m", Some(s"item $m"), n, m)),
        LocalDate.now().minusDays(n)
      ))
      val uuids = mutable.MutableList[UUID]()
      for (view <- billViews) {
        Post("/api/v1/bill", view) ~> route ~> check {
          println(responseAs[String])
          Get(s"/api/v1/bill/${responseAs[String]}") ~> route ~> check {
            val res = responseAs[BillResponse]
            res.bill.name shouldBe view.name
            uuids += res.id
          }
        }
      }
    }
  }
}
