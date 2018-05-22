package ru.fintech.school.ubilling.telegram

import java.time.LocalDate
import java.util.UUID

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.models.Message
import ru.fintech.school.ubilling.BillApp
import ru.fintech.school.ubilling.domain.{BillView, BillViewItem}
import ru.fintech.school.ubilling.schema.TableDefinitions
import ru.fintech.school.ubilling.schema.TableDefinitions.BillId

import scala.concurrent.Future


object Bot extends TelegramBot
  with Commands
  with Polling
  with Callbacks {
  override def token: String = "567110199:AAFCzng2HxEZTTE37AHWRwmt1xnex_j87GM"

  val baseUrl = "http://localhost:8080"


  onCommand("/start", "/menu", "/slyshrabotat") { implicit msg =>
    createUserIfDoesntExist(msg)
    reply("Enter command: /bills or /new or /getbill")
  }

  private def createUserIfDoesntExist(implicit msg: Message) = {
    userExists(msg.from.get.username.getOrElse(msg.from.get.id.toString)).onComplete(userExists => {
      if (!userExists.getOrElse(false)) {
        if (msg.from.get.username.isDefined) {
          createUser(msg.from.get.username.get)
        } else {
          reply("Create username please")
        }
      }
    })
  }

  onCommand("/bills") { implicit msg =>
    createUserIfDoesntExist(msg)
    getAllBills(msg.from.get.username.get).onComplete(billSeq => {
      val message = billSeq.getOrElse(Seq.empty).flatMap(billView => ("\n=====" + billView.name + "=====") +: billView.items.map(billViewItem => s"${billViewItem.name} ${billViewItem.description.getOrElse("")}. ${billViewItem.count} pieces for ${billViewItem.price} each.")).mkString("\n")
      if (message.length == 0)
        reply("No bills yet")
      else
        reply(message)
    })
  }

  onCommand("/new") { implicit msg =>
    createUserIfDoesntExist(msg)
    if (msg.text.get == "/new")
      reply("Please use the following format:\n/new bill name\nname description count price")
    else {
      val text = msg.text.get.substring(5).split("\n")
      postBill(text.head, text.tail, msg.from.get.username.get).onComplete(id => {
        reply("Added new bill. You can share it by ID:").onComplete(_ => {
          reply(id.get.getOrElse("lul").toString)
        })
      })
    }
  }

  onCommand("/getbill") { implicit msg =>
    createUserIfDoesntExist(msg)
    val messageText = msg.text.getOrElse("")
    if (messageText == "/getBill")
      reply("Please use the following format:\n/getBill BillID")
    else {
      val billId = messageText.substring(messageText.indexOf(" ") + 1)
      assignBillToUser(UUID.fromString(billId), msg.from.get.username.get)
      reply("The bill is assigned.")
    }
  }

  def userExists(name: String) = {
    BillApp.userService.findUserByEmail(name).map(_.isDefined)
  }

  def getUserId(name: String) = {
    BillApp.userService.findUserByEmail(name).map(_.get.uid)
  }

  def createUser(name: String) = {
    BillApp.userService.addUser(name, Option.empty)
  }

  def getAllBills(name: String) = {
    val uid = BillApp.userService.findUserByEmail(name).map(_.get.uid)
    uid.flatMap(BillApp.userBillService.findBillIds)
      .flatMap(idSeq => Future.sequence(idSeq.map(id => BillApp.billService.findBill(id))))
      .map(_.collect({
        case x: Some[BillView] => x.get
      }))
  }

  def postBill(name: String, items: Seq[String], userName: String) = {
    val parsed = items.map({ it =>
      val Array(name, description, count, price) = it.split(" ")
      BillViewItem(name, Option(description), count.toInt, price.toInt)
    })
    val billID = BillApp.billService.addBill(BillView(name, parsed, LocalDate.now))
    billID.onComplete(billID => assignBillToUser(billID.get.get, userName))
    billID
  }

  def assignBillToUser(bill: TableDefinitions.BillId, user: String) = {
    getUserId(user).onComplete(userId => BillApp.userBillService.assignBill(userId.getOrElse(0), bill))
  }

  def showBill(id: TableDefinitions.BillId) = {
    BillApp.billService.findBill(id).map(_.get.items.map(item => item.toString))
  }

}
