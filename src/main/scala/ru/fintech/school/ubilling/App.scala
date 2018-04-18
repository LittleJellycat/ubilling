package ru.fintech.school.ubilling

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import ru.fintech.school.ubilling.handler.BillServiceImpl
import ru.fintech.school.ubilling.handler.InMemoryBillDao
import ru.fintech.school.ubilling.routing.BillRouting

import scala.concurrent.ExecutionContextExecutor

object BillApp extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val billService = new BillServiceImpl(InMemoryBillDao)
  val routes = BillRouting.route(billService)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}