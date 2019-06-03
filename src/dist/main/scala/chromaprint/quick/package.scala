package chromaprint

import scala.concurrent.ExecutionContext.global

import cats.effect.{ContextShift, IO, Resource}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

package object quick {

  implicit lazy val cs: ContextShift[IO] =
    IO.contextShift(global)

  implicit lazy val fftImpl: FFT =
    breeze.FFTImpl

  implicit lazy val httpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global).resource
}
