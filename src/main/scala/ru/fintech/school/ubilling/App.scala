package ru.fintech.school.ubilling

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import ru.fintech.school.ubilling.dao.UserBillDAL
import ru.fintech.school.ubilling.dao.UserBillDAL._
import ru.fintech.school.ubilling.handler.{BillServiceImpl, UserBillServiceImpl, UserServiceImpl}
import ru.fintech.school.ubilling.routing.AppRouting

import scala.concurrent.ExecutionContextExecutor

object BillApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  dropSchema.onComplete(_ => createSchema)
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val billService = new BillServiceImpl(UserBillDAL)
  val userService = new UserServiceImpl(UserBillDAL)
  val userBillService = new UserBillServiceImpl(UserBillDAL)
  val routes = AppRouting.route(billService, userService, userBillService)
  Http().bindAndHandle(
    routes, config.getString("http.interface"), config.getInt("http.port")
  )
}