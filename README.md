# ZeText: Zero disk exposition texts

This is a simple text encryption/decryption password based GUI+CLI tool, allowing to enter, edit and decrypt files in a way these never hit the filesystem. The ecnryption is robust (AES256) so is the password key derivation (PBKDF< HMAC on SHA3_384, manu rounds), so unless you use predicatble password, the encrypted text is safe.

The main purpose of this tool is to protect _distribution generation scripts_. As we all know, the probability that our file system is silently scanned by troojans and vurises is really high, so whatever secret is on our FS will someday be sold in the darknet. This open the door for fearful supply chain attack when our stolen, say. maven credentials are used to publish modified version of our stuff. We protect from it by putting such materials under the password, but we can not use traditional unix way tools, as even if our credentials will be decrypted for a split second while build scripts are active, it is enough for file-stealing trojans and viruses.

Say, we use maven publich plugin, so narutally we'll have in our build gradle script something like:

~~~
credentials {
    username System.getProperty("user")
    password System.getProperty("password")
}
~~~

That is fine, but we can't save these environment variables to a file without risk to cmopromise our deployment. That's where zetext comes to help.

We create new zetext, say, `mcreds.ztext`, with the following content (example):

~~~
export user="my_maven_user"
export password="my_maven_password"
#
# whatever else we want tp do before publishing
#
~~~

As zetext never saves plaintext to disk, it is kept safe under our password. Now, in our build script we but something like:

~~~
source <(zetext -d mcres.ztext)
./gradlew publish
~~~

The first line executes zetex in decryption mode: it opens GUI window, asks for password and decrypts the text to stdout. The decrypted text is passed to the shell's source (without hitting the disk) and interpreted in the context of the current shell process, where the environment verialbles above will be exported witout hitting the disk. Then the `gradle publish` task will perform having password and user set to correct values, until the calling script is dinished, where it will be more or less safe dropped. 

If the password is wrong, or user has cancelled it, ztext returns error exit status (100 for cancel and 101 for wrong file/password).

This method has several advantages over setting up environments variables manually:

- password is validated before build and can be reentered if mistaken
- file contents are authenticated and could not be modified without password (EtA mode) 
- password and other credentials didn't get into bash history even by mistake
- it is possible to put many secret data in a single file and remember/type only one password

## How to install

I'm working on providing binary installation, but until then, clone the reposittory, install JDK and gradle and whatever else `./gradlew createDistributable` will ask. Then start the app that will be in `${projectRoot}/build/compose/binaries/main`. On the first start, select form the application menu tools -> install zetext shell script. Also, you can use `.gradlew package` to create installatino for your platform, that will also be placed somewhere in `${projectRoot}/build/compose/binaries/` depending on your platform. 

Script will be installed to `/usr/local/bin` if exists and writable, otherwise to `~/bin`.

