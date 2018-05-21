//package ru.fintech.school.ubilling.telegram
//
//import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
//import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
//import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup}
//
//
//import dispatch._, Defaults._
//import scala.util.{Success, Failure}
//
//object Bot extends TelegramBot
//  with Commands
//  with Polling
//  with Callbacks {
//  override def token: String = "567110199:AAEwv8vbEsJN5cXvKW6tk2502CNMQ6Ih42Q"
//
//  val baseUrl = "http://localhost:8080"
//
//  val TAG = "COUNTER_TAG"
//
//  def tag = prefixTag(TAG) _
//
//  onCommand("/start", "/menu", "/slyshrabotat") { implicit msg =>
//    if (!userExists(msg.from.get.username.getOrElse(msg.from.get.id.toString))) {
//      if (msg.from.get.username.isDefined) {
//        createUser(msg.from.get.username.get) // actually, api uses email
//      } else {
//        createUser(msg.from.get.id.toString)
//      }
//    }
//    reply("Select action", replyMarkup = Option(menuLayout))
//  }
//
//
//  def menuLayout = InlineKeyboardMarkup.singleRow(List(
//    InlineKeyboardButton.callbackData("List bills", "LIST"),
//    InlineKeyboardButton.callbackData("New bill", "NEW")
//  ))
//
//  def listBillsLayout(bills: Seq[String]) = {
//    InlineKeyboardMarkup.singleColumn(bills.take(5).map(bill => InlineKeyboardButton.callbackData(bill, "FIND")))
//  }
//
//  //
//  //  def listPositions(positions: Seq[String]) = { implicit msg =>
//  //    reply(positions.mkString("\n"))
//  //  }
//
//  //  onCallbackWithTag(TAG) { implicit cbq =>
//  //    ackCallback(cbq.)
//  //  }
//
//  def createUser(name: String) = {
//    val svc = url(s"$baseUrl/api/v1/user/") //???
//    def myPostWithParams = svc << Map("key" -> "value") //???
//    val response: Future[String] = Http(svc OK as.String)
//    response onComplete {
//      case Success(content) => {
//        logger.info(s"User $name created")
//      }
//      case Failure(t) => {
//        logger.error(t.getMessage)
//      }
//    }
//  }
//
// // def getUser() = ??? //???
////  def getUserBills(userId: String): Future[Any] = {
////    val
//////    val svc = url(s"$baseUrl/api/v1/user/") //???
//////    val response: Future[String] = Http(svc OK as.String)
//////    response onComplete {
//////      case Success(content) => {
//////        logger.info(s"User $name created")
//////      }
//////      case Failure(t) => {
//////        logger.error(t.getMessage)
//////      }
//////    }
////  }
//
////  def getBillNames(user: String) = {
////    val svc = url(s"$baseUrl/api/v1/user/$user/bills/")
////    val response: Future[String] = Http(svc OK as.String)
////    response.onComplete{
////
////      val svc = url(s"$baseUrl/api/v1/bill/")
////    }
////
////  }
//
//  def getBill() = ???
//
//  def postBill() = ???
//
//  def assignBillToUser() = ???
//
//  def getBillPhoto() = ???
//
//  def postBillPhoto() = ???
//
//  def userExists(id: String): Boolean = {
//    val svc = url(s"$baseUrl/api/v1/user/$id") //???
//    val response: Future[String] = Http(svc OK as.String)
//    response onComplete {
//      case Success(_) => {
//        return true
//      }
//      case Failure(t) => {
//        logger.error(t.getMessage)
//      }
//    }
//    false
//  }
//
//  //def editBill() = ???
//
//
//  //def deleteBill() = ???
//
//}
//
////val svc = url("http://www.wikipedia.org/");
////val response : Future[String] = Http(svc OK as.String)
////
////response onComplete {
////  case Success(content) => {
////  println("Successful response" + content)
////}
////  case Failure(t) => {
////  println("An error has occurred: " + t.getMessage)
////}
////}
//
////post user
////get user
////get all bills
////assign bill
////get photo
////post photo