package rockthejvm.comicbookactors

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}
import rockthejvm.comicbookactors.Main_Section_2.{avengersActors, jlActors}
import rockthejvm.comicbookactors.Model.Actor

import scala.::

// https://blog.rockthejvm.com/fs2/

object Main_Section_3 extends IOApp {

  // Concatenate two streams
  // "will emit all the actors from the Justice League and then the Avengers."
  val dcAndMarvelSuperheroes: Stream[Pure, Actor] = jlActors ++ avengersActors

  // Stream type forms a Monad on the O type parameter so it can be flat mapped (etc.) over e.g.
  val printedJLActors: Stream[IO, Unit] = jlActors.flatMap { actor =>
    Stream.eval(IO.println(actor))
  }

  // "The pattern of calling the function Stream.eval inside a flatMap is so common that fs2 provides
  // a shortcut for it through the evalMap method:"
  val evalMappedJLActors: Stream[IO, Unit] = jlActors.evalMap(IO.println)

  // "If we need to perform some effects on the stream, but we don’t want to change the type of the stream,
  // we can use the evalTap method:"
  // I.e. use IO.println but the stream continues to output Actor instead of Unit (no mapping)
  val evalTappedJLActors: Stream[IO, Actor] = jlActors.evalTap(IO.println)

  // group the Avengers cast by the actors first name
  val avengersByFirstName: Stream[Pure, Map[String, List[Actor]]] =
    avengersActors.fold(Map.empty[String, List[Actor]]) { (map, actor) =>
      map + (actor.firstName -> (actor :: map.getOrElse(actor.firstName, Nil)))
    }

  override def run(args: List[String]): IO[ExitCode] = {
//    printedJLActors.compile.drain.as(ExitCode.Success)
//    evalMappedJLActors.compile.drain.as(ExitCode.Success)
    evalTappedJLActors.compile.drain.as(ExitCode.Success)
  }

}
