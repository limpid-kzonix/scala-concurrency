

object Main {

  def main(args: Array[String]): Unit = {
    // Create Drop
    val pipe = Pipe.apply

    // Spawn Producer
    new Thread(new Producer(pipe)).start()

    // Spawn Consumer
    new Thread(new Consumer(pipe)).start()
  }
}

object Pipe {
  def apply: Pipe = new Pipe()
}
class Pipe() extends Comunicatable {
  var message : String = ""
  var empty : Boolean = true
  var lock : AnyRef = new Object()

  def put(x: String) : Unit =
    lock.synchronized
    {
      await (empty == true)
      empty = false
      message = x
      lock.notifyAll()
    }

  def take() : String =
    lock.synchronized
    {
      await (empty == false)
      empty=true
      lock.notifyAll()
      message
    }

  private def await(cond: => Boolean) =
    while (!cond) {
      Console.println(Thread.currentThread())
      lock.wait()
    }
}

class Producer(drop : Pipe)
  extends Runnable
{
  // Can be specific storage of data
  val storage : Array[String] = Array(
    "First message",
    "Second message",
    "Third message",
    "Last message"
  );

  override def run() : Unit =
  {
    Console.println(s"PRODUCER: ${Thread.currentThread()}")
    storage.foreach((msg) => {
      Thread.sleep(5000)
      Console.println(s"SENT - $msg")
      drop.put(msg)
    })
    drop.put("Finish")
  }
}

class Consumer(drop : Pipe)
  extends Runnable
{
  override def run() : Unit =
  {
    Console.println(s"CONSUMER: ${Thread.currentThread()}")
    var message = drop.take()
    while (message != "Finish")
    {
      Console.println(s"RECEIVED - $message")
      message = drop.take()
    }
  }
}

trait Comunicatable
