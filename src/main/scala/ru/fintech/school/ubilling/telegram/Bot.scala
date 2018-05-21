package ru.fintech.school.ubilling.telegram

import java.time.LocalDate

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import ru.fintech.school.ubilling.BillApp
import ru.fintech.school.ubilling.domain.{BillView, BillViewItem}
import ru.fintech.school.ubilling.schema.TableDefinitions


object Bot extends TelegramBot
  with Commands
  with Polling
  with Callbacks {
  override def token: String =

  val baseUrl = "http://localhost:8080"


  onCommand("/start", "/menu", "/slyshrabotat") { implicit msg =>
    if (!userExists(msg.from.get.username.getOrElse(msg.from.get.id.toString))) {
      if (msg.from.get.username.isDefined) {
        createUser(msg.from.get.username.get)
      } else {
        createUser(msg.from.get.id.toString)
      }
    }
    reply("Enter command: /bills or /new")
  }

  onCommand("/bills") { implicit msg =>
    reply(getAllBills(msg.from.get.username.get).mkString("\n"))
  }

  onCommand("/new") { implicit msg =>
    val text = msg.text.get.split("\n")
    postBill(text.head, text.tail)
  }

  def userExists(name: String): Boolean = {
    BillApp.userService.findUserByEmail(name).value.map(_.get.isDefined).getOrElse(false)
  }

  def getUserId(name: String) = {
    BillApp.userService.findUserByEmail(name).value.get.get.get.uid
  }

  def createUser(name: String) = {
    BillApp.userService.addUser(name, Option.empty)
  }

  def getAllBills(name: String) = {
    val uid = BillApp.userService.findUserByEmail(name).value.get.get.get.uid
    BillApp.userBillService.findBillIds(uid).value.get.getOrElse(Seq.empty)
      .map(id => BillApp.billService.findBill(id).value.get.get.get)
  }

  def postBill(name: String, items: Seq[String]) = {
    val parsed = items.map({ it =>
      val Array(name, description, count, price) = it.split(" ")
      BillViewItem(name, Option(description), count.toInt, price.toInt)
    })
    BillApp.billService.addBill(BillView(name, parsed, LocalDate.now))
  }

  def assignBillToUser(bill: TableDefinitions.BillId, user: String) = {
    BillApp.userBillService.assignBill(getUserId(user), bill)
  }

  def showBill(id: TableDefinitions.BillId) = {
    BillApp.billService.findBill(id).value.get.get.get.items.map(item => item.toString)
  }

}
