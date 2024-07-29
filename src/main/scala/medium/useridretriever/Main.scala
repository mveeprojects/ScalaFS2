package medium.useridretriever

import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import fs2._

import scala.concurrent.Future

// https://medium.com/free-code-camp/a-streaming-library-with-a-superpower-fs2-and-functional-programming-6f602079f70a

object Main extends IOApp {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private def loadUserIdByName(userName: String): Future[Long] = userName match {
    case "bob"   => Future(1234L)
    case "alice" => Future(4321L)
    case _       => Future(5678L)
  }

  //         Stream[Effect, Output]
  val names: Stream[Pure, String] = Stream("bob", "alice", "joe")

// still evaluates as a Pure effect with map, need to use evalMap instead to signal there is an effect from the loadUserIdByName function.
//  val userIdsFromDB: Stream[Pure, Future[Long]] = names.map(loadUserIdByName)

  // now the effect is not "Pure" but "Future", with an output of Long based on the return type of loadUserIdByName
  // "It separated the Future and isolated it! The left side that was the Effect type parameter is now the concrete Future type. [separation of concerns]"
  val userIdsFromDB: Stream[IO, Long] = names.evalMap { name =>
    IO.fromFuture(IO(loadUserIdByName(name)))
  }
  // Note: needed to change to IO as there is no required implicit Compiler in FS2 these days, IO has the required Compiler/Sync thing.
  // Source: https://stackoverflow.com/a/62301038/3059314

  override def run(args: List[String]): IO[ExitCode] = {
    userIdsFromDB.compile.toList.unsafeRunSync.foreach(println)
    IO(ExitCode.Success)
  }
}
