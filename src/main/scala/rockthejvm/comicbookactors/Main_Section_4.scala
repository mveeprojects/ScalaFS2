package rockthejvm.comicbookactors

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import rockthejvm.comicbookactors.Main_Section_2.jlActors
import rockthejvm.comicbookactors.Model.Actor

import scala.util.Random

// https://blog.rockthejvm.com/fs2/#4-error-handling-in-streams

object Main_Section_4 extends IOApp {

  // No error handling
  val savedJLActors: Stream[IO, Int] = jlActors.evalMap(ActorRepository.save)

  // Basic error handling
  val savedJLActorsWithErrorHandling: Stream[IO, AnyVal] =
    savedJLActors.handleErrorWith(error => Stream.eval(IO.println(s"Error: ${error.getMessage}")))

  // Error handling to Either
  val attemptedSavedJlActors: Stream[IO, Either[Throwable, Int]] = savedJLActors.attempt

  override def run(args: List[String]): IO[ExitCode] =
//    savedJLActors.compile.drain.as(ExitCode.Success)

//    savedJLActorsWithErrorHandling.compile.drain.as(ExitCode.Success)

    attemptedSavedJlActors.evalMap {
      case Left(error) => IO.println(s"Error: ${error.getMessage}")
      case Right(id)   => IO.println(s"Saved actor with id: $id")
    }.compile.drain.as(ExitCode.Success)

  // Could you combine the use of evalTap and handling in some way to prevent the stream ending,
  // but logging the problematic cases out via the tap?
}

object ActorRepository {
  def save(actor: Actor): IO[Int] = IO {
    println(s"Saving actor: $actor")
    if (Random.nextInt() % 2 == 0) {
      throw new RuntimeException("Something went wrong communicating with the persistence layer.")
    }
    actor.id
  }
}
