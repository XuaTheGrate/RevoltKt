.. raw:: html

    <h1 align="center">RevoltKt</h1>

.. raw:: html

    <h3 align="center">A pure-Kotlin library for bots to interface with Revolt</p>
    <p align="center">
        <img alt="GitHub" src="https://img.shields.io/github/license/XuaTheGrate/RevoltKt">
        <img alt="GitHub release (latest by date including pre-releases)" src="https://img.shields.io/github/v/release/XuaTheGrate/RevoltKt?include_prereleases">
        <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/XuaTheGrate/RevoltKt?style=social">
    </p>

----------

Sample Usage
------------
.. code-block:: kotlin

    import me.maya.revolt.defaultClientBuilder
    import me.maya.revolt.events.Event
    import me.maya.revolt.events.EventHandler
    import me.maya.revolt.registerEventHandler
    import me.maya.revolt.setErrorCallback
    import me.maya.revolt.token

    val token = System.getenv("TOKEN")

    fun main() {
        val client = defaultClientBuilder {
            token(token)

            registerEventHandler(object: EventHandler() {
                override suspend fun onMessage(event: Event.Message) {.
                    println("Received ${event.message.content} from ${event.message.author.username}")

                    if (event.message.author.id == "my_owner_id") client.close()
                }
            })

            setErrorCallback { event, error ->
                println("Error occured in event $event")
                error.printStackTrace()
            }
        }.build()
        client.runForever()
    }

Installation
------------
Gradle is confusing. I know, I've used it for too long.
You can download my `sample repo <https://github.com/XuaTheGrate/RevoltKt_template>`_ for quick setup and use!

Or if you'd rather, you can download the artifacts from `GitHub releases <https://github.com/XuaTheGrate/RevoltKt/releases>`_ and add them manually:

.. code-block:: kotlin

    dependencies {
        implementation(files("Revolt-0.2.0-full.jar"))
        // `full` contains all dependencies (ktor, kotlin-reflect etc)
        // If needed, you can use `Revolt-0.2.0.jar` for a version without dependencies
    }

Support
-------
I have a `Revolt support server <https://app.revolt.chat/invite/9QyEK9R4>`_ you are welcome to join and ask for help. There I will post announcements as well.

Contributing
------------
This project is completely open source! As long as you have an idea, feel free to make a pull request. Be sure to test it before hand.

See Also
--------
- `The official Revolt GitHub organization <https://github.com/revoltchat>`_
- `Awesome Revolt for a list of other libraries, bots, clients and more <https://github.com/insertish/awesome-revolt>`_
- `The official Revolt testers server <https://app.revolt.chat/invite/Testers>`_

