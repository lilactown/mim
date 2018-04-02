# mim

A powerful task runner, built for clojure.

## Installation

For now, clone this repo and run `./install.sh`

## Usage

Once you've gone through the installation steps, you may run `mim` from a
terminal to see usage instructions.

The basic idea is that, in your project (or wherever) you create a `mim.edn`
file. This file should be a map of keys and values that you can use to reference
through the CLI tool.

Example 1:

```clojure
{:foo {:bar "baz"}}
```

Now when you run `mim foo bar` in the same directory as this file, the app:

0. (Optional) Starts the mim server
1. Loads the `mim.edn` file
2. Obtains value contained at the path passed to the `mim` command
3. Evaluates the value as a Clojure form

In this case, the app evalutes "baz" and simply exits, without outputting
anything.

```
$ mim foo bar
:mim/exit 0
```

If we want to output something, we simply print to `*out*`:

```clojure
{:foo {:bar (println "baz)"}}
```

```
$ mim foo bar
baz
:mim/exit 0
```

Now, let's do something kind of useful. Mim comes built in with a `mim/task`
function that allows us to easily run commands from the shell:

```clojure
{:home (mim/task "ls ~")}
```

```
$ mim home
:mim/exit 0
emacs-mac       lilactown       mim
clojure         js       
```

We can also pass some configuration to the mim/task command, such as the
directory we want the shell command to run in:

```clojure
{:home (mim/task "ls"
                 :cwd "~")}
```

```
$ mim home
:mim/exit 0
emacs-mac       lilactown       mim
clojure         js       
```

### Trampolining

By default, `mim/task` "trampolines" the shell command passed to it. What this
means in the context of mim, is that instead of running the shell command within
the mim _server_ process, it instead instructs the client to spawn it and run it
as a child of the calling shell. This allows the thread to die on the mim server
and prevents some complexity/memory leaks.

Non-trampolined tasks are coming *SOON*

## TODO

 - [ ] Owned (non-trampolined) tasks
 - [ ] Environment variables
 - [ ] Concurrent tasks
 - [ ] Examples using conch
 - [ ] Allow a mim.edn form to reference other key paths
 - [ ] GUI??

## License

Copyright © 2018 Will Acton

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
