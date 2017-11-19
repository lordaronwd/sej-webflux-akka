package com.zooplus.sej.msone

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.zooplus.sej.msone.UserActorSupervisor.CreateUser

/**
  * @author lazar.agatonovic
  */
object UserActorSupervisor {

  def props(userRepository: UserRepository): Props = Props(new UserActorSupervisor(userRepository))

  case class CreateUser(user: User)
  case class UpdateUser(user: User)
  case class GetUser(client: ActorRef)
}
class UserActorSupervisor(userRepository: UserRepository) extends Actor with ActorLogging {

  var refs: Map[Long, ActorRef] = Map.empty

  override def receive: Receive = {
    case CreateUser(user: User) =>
      getUserActor(user.getId) ! UserActor.CreateUser(sender(), user)
    case _ =>
      throw new IllegalArgumentException("Unknown message")
  }

  def getUserActor(userId: Long): ActorRef = {
    refs.getOrElse(userId, {
      val actor = context.actorOf(UserActor.props(userRepository), s"UserActor-$userId")
      refs = refs.updated(userId, actor)
      return actor
    })
  }
}
