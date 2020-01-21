package org.gfccollective.aws.kinesis.akka

import scala.concurrent.duration.{Duration, FiniteDuration}

case class RetryConfig(
  initialDelay: Duration,
  retryDelay: FiniteDuration,
  maxRetries: Int
)
