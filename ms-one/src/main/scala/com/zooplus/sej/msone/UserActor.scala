package com.zooplus.sej.msone

import java.util.concurrent.CompletionStage

import akka.actor._
import akka.pattern.pipeCompletionStage
import com.zooplus.sej.msone.UserActor.{CreateUser, UserResponse}
import org.springframework.http.HttpStatus

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

object UserActor {

  implicit def toScalaFuture[I](cs: CompletionStage[I]) : Future[I] =  FutureConverters.toScala(cs)

  def props(userRepository: UserRepository): Props = Props(new UserActor(userRepository))

  sealed trait UserMessage {
    def client: ActorRef
    def user: User
  }

  case class CreateUser(client: ActorRef, user: User) extends UserMessage
  case class UpdateUser(client: ActorRef, user: User) extends UserMessage
  case class GetUser(client: ActorRef)
  case class UserResponse(user: User, status: HttpStatus)
}

class UserActor(userRepository: UserRepository) extends Actor with ActorLogging with Stash {

  var userEntity: User = _

  override def receive: Receive = waitingForRequests

  def waitingForRequests: Receive = {
    case CreateUser(client, user) =>
      log.info("Received create message")
      if (Option(userEntity).nonEmpty) {
        client ! UserResponse(userEntity, HttpStatus.OK)
      } else {
        userRepository.createUser(user) pipeTo context.self
        context become waitingForResponse(client)
      }
  }

  def waitingForResponse(originalSender: ActorRef): Receive = {
    case u: User =>
      userEntity = u
      originalSender ! UserResponse(u, HttpStatus.CREATED)
      unstashAll()
      context become waitingForRequests
    case _ =>
      stash()
  }

}