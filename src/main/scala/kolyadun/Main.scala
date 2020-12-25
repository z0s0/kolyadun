package kolyadun

import kolyadun.config.ScenariosConfig
import kolyadun.service.ScenariosCollector
import kolyadun.service.ScenariosCollector.ScenariosCollector
import org.slf4j.LoggerFactory
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{App, ExitCode, Has, UIO, URIO, ZEnv, ZIO, ZLayer}
import zio.console.Console
import zio.internal.Platform

object Main extends App {
  private val log = LoggerFactory.getLogger("RuntimeReporter")
  override val platform: Platform = Platform.default.withReportFailure {
    cause =>
      log.error(cause.prettyPrint)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val program = for {
      service <- ZIO.access[ScenariosCollector](_.get)
      _ <- service.collect
    } yield ()

    val live =
      (AsyncHttpClientZioBackend.layer() ++ ScenariosConfig.live) >>>
        ScenariosCollector.live
    program.provideLayer(live).exitCode
  }
}