object ScodecGlue {

  private val MaxMessageLength = 2048

  def decoder[T](codec: Codec[T]): Flow[ByteString, Try[T], NotUsed] =
    Flow[ByteString].via(Framing.simpleFramingProtocolDecoder(MaxMessageLength))
      .map { frame =>
        codec.decode(BitVector(frame.toByteBuffer)) match {
          case Attempt.Successful(t) => Success(t.value)
          case Attempt.Failure(cause) => Failure(new RuntimeException(s"Unparseable command: $cause"))
        }
      }

  def encoder[T](codec: Codec[T]): Flow[T, ByteString, NotUsed] =
    Flow[T].map { t =>
      codec.encode(t) match {
        case Attempt.Successful(bytes) => ByteString(bytes.toByteBuffer)
        case Attempt.Failure(error) => throw new RuntimeException(s"Failed to encode $t: $error")
      }

    }.via(Framing.simpleFramingProtocolEncoder(MaxMessageLength))

}
