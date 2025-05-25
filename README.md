# Infinitode 2 Wheel Solver

An app to do lucky wheel RNG manipulation on Infinitode 2.

Written for Infinitode 2 R.1.9.2.
There's a high probability that it will not work on any other version of Infinitode 2.

## ℹ Preamble
Inspired by [this video](https://youtu.be/XcB8cddYiwY) and others like it, this is a tool that helps you manipulate the lucky wheel RNG to get the items you want.
 
The technique displayed in the video is a neat trick, but with the 60-second delay after reloading from cloud, it starts to become time-consuming to go through very many possibilities, and difficult to keep track of which ones you've tried.

So, this tool does the same thing, but automatically.
Provide it with the Infinitode 2 data and jar (you can copy them from your Steam folder) and it will hook into the game and ask it to load your local progress.
Then, it will spin the wheel, and see what items you get.
According to some configurable parameters, it will assign a score to the result, and try a different sequence of respins.
At the end, it will show you the best options.

## ⛔ Infinitode rules

Note that you will need to have your progress locally in order to perform this. (You can find it in your Steam folder.)
This is partly because it's easier that way, but mostly because of Infinitode's rule 0.
Take a moment to read [Infinitode's terms and conditions](https://infinitode.prineside.com/?m=game_terms_and_conditions) if you haven't.
Don't worry, they're much easier on the eyes than most other terms and conditions you might see.

Rule #0 states that you should not do anything that messes with the Infinitode servers, and this project respects that.
If you're curious, here's a breakdown of the specific things this app does and doesn't do with regard to the rules.

- **"Avoid third party software which forges in-app purchases"**
  - ✅ This app has nothing to do with in-app purchases.
- **"Avoid auto-clickers and any other method of constantly replaying the same levels over and over again."**
  - ✅ This app does not play levels, and thus does not send replays to the server.
- **"Do not upload NSFW and provocative profile pictures, use appropriate nickname."**
  - ✅ This app does not modify your user data
- **"Don't do anything that may harm our servers - such things as traffic spamming, password bruteforcing and any other kind of abuse."**
  - ✅ Obviously this is very broad, but the app takes steps to make sure **almost no traffic at all** is sent to the Infinitode servers. The game does a few things automatically that are more difficult to control, but the important bits are removed. Most notably, before launching Infinitode, this app deletes the Infinitode API URL from memory, so any requests to its API will automatically fail. 
 
If you're at all concerned, please disconnect your internet connection while using the app; it will work perfectly fine offline.

## 🛠 Using the app

Build the app with `./gradlew build`. It will appear in `build/libs/`.

Once you've built the app, make a copy of your Infinitode data from your Steam folder and put it in a folder with this app.
The app avoids writing any changes to your save files, but it's best to make a copy just in case.

The files/folders you will need are:
- `res/`: Contains lots of game resources, like tower stats and the research tree. As such, the game won't start without it.
- `i18n/`: Required in order to display the names of items.
- `saves/`: Your save files contain several important pieces of information:
  - Your current lucky wheel RNG
  - The number of accelerators and lucky tickets you have to spend
  - What research you have completed and what items you already have (lucky wheel choices depend on these)
  - Maybe more that the game reads behind the scenes?

## 🤔 How it works

Infinitode 2 is written in Java, so this application is also written in Java, letting them link directly together.
Basically, the application starts Infinitode but tells it to load the minimum required for the game to run.
Then, the application loads a few technically non-essential parts of the game, such as inventory management.

Next, the application simulates the creation of a lucky wheel spinner (which is not very straightforward, due to the fact that we're creating UI components without an actual UI.) 
After that, we're ready to roll (or spin)! The `WheelWrapper` class provides an interface to the complex underlying functions.

The application uses that wheel wrapper and an algorithm to see what happens each time we spin the wheel.
It's essentially a tree of available options.
At any time, you can either purchase a respin of the existing wheel, or buy a new wheel, though sometimes only one (or none) of those options is available.

Once the algorithm reaches a point where it has no more options, it stops, saves the steps taken and items received, and reloads progress from the save file to try another path through the tree.
Once it's exhausted all possible options, it sorts the options from best to worst, and displays them.

### Algorithms

There are actually two algorithms included in the application: linear and recursive.
Linear is much slower but more reliable; it tells the game to reset your inventory and progress to what's in your save file, and only spins the wheel in linear sequences.
Recursive is much faster but more likely to be inaccurate; it replaces some of the game code to be able to quickly roll back the user's inventory, and maintains many game states simultaneously for efficiency.

You can think of the linear one more like a sledgehammer; there's little doubt that it will get the job done, but it's more unwieldy.
The recursive one is more like a scalpel, it's precise and efficient, but only if you know how to use it; it's possible there are edge cases that it doesn't handle.

## 💎 Understanding the output

Here's an abridged output from one of the runs I did:

```
...
Finished exploring possibilities, got 8 useful results (60 total)
100.0: [RNRNRNRR] 2x Research ticket (10x Cyan key, 1x Type barrier, 1x Green chest, 3x SPECIAL I, 2x Source, 10x AGILITY, 250x Tensor)
75.0: [RNRRNNRR] 2x Research ticket (1x Type barrier, 10x Cyan key, 2x Source, 10x AGILITY, 250x Matrix, 250x Tensor, 10x Random tile)
60.0: [RNNRRRNRR] 2x Research ticket (2x Source, 1x Type barrier, 1x Green chest, 12x SPECIAL I, 10x Bit dust, 10x AGILITY, 250x Tensor)
...
```

The first line of the output tells you how many paths it tried to get the results.
"Useful" paths are paths that have at least one desired item in them.
Paths that are not determined to be useful are not displayed.

The leftmost column in each of the following lines the score assigned to this outcome.
It's configurable how that's calculated, but in this run it's calculated as `(research tickets * 100) - accelerator cost`.

The second column, enclosed in square brackets, is the steps required to get to this output.
`R` means "respin", and `N` means "new wheel".
Easy!

> Note: these correspond 1:1 to the buttons you should hit on the interface; there's no indicator for respinning after landing on a multiplier because there's no button for that, you just spin again.
In other words, `NR` means you should do "purchase wheel" followed by "purchase respin", not just "purchase wheel" and then use your free spin. 

The third column, outside any brackets, is the list of desired items that would be acquired from doing these steps.

The last column, in parenthesis, is the list of non-desired items that would be acquired.

## 🛣 Roadmap

The app works as-is, but there are a few things I think would be cool to add/improve:
- Reduce the flood of log messages that the game produces while spinning the wheel.
- Configure item weights without recompiling.
- Add an option to allow purchasing new wheel spins using lucky tickets.
  - I'm not very far into endless yet, so accelerators are still more accessible than lucky tickets for me. I'm aware that's not the case the farther you get into endless mode, so I'll add that at some point.
- Improve performance, see below
  - ~~Faster options for resetting progress~~
  - Multiple paths with the same resulting RNG state (hard)
  - Parallel processing and saving path progress
- Maybe a config GUI at some point?

## ⏰ Performance

The time complexity of this algorithm is inherently pretty large, since you have two options (respin or new wheel) after each spin until you run out of tickets or accelerators.
And indeed, it's not very fast.
It took ~9 minutes to do a run with ~400 accelerators and 11 tickets on fairly recent hardware.
Time required decreases dramatically as the number of tickets is decreased.

There are a few optimizations in the code now, but not many.
For example, currently the app will never try to respin the last wheel if there's no items of interest on it.

It can certainly be improved; the current algorithm is nearly just brute force.
There are at least a few shortcuts I know of already that it could be taking advantage of:

### Multiple paths with the same resulting RNG state (hard)
The lucky wheel can be abstracted as a tree, but it's really more complicated than that.
There are actually two RNGs for the lucky wheel: one deals with wheel generation, and one deals with how far the weapon and wheel turn each time you spin.

The spinner RNG is only updated when it's used, so it will end up in the same state after three spins, regardless of whether the spin happened because of a purchased respin, purchased new wheel, or just landing on an x2 or x3.
This is not the case with the wheel generation RNG, however.

This means that the paths `RRNRN` and `RNRRN` will wind up in the same state at the end, since the spinner RNG and wheel RNG each moved forward the same number of times.
Note that this isn't the case for paths `RNR` and `RRN`, since even though the spinner will move the same amount in both paths, the weapon and wheel always start at an angle of 0 when buying a new wheel.
Note also that it doesn't mean `RRNRN` and `RNRRN` will get you the same items (they won't), it just means that any items received *after* those paths will be the same.

However, there's another confounding factor: unfinished research.
When building the wheel, the game looks to see if you have any unfinished research you can't afford.
If so, it will give extra priority to the items required for that research.

This makes it more complicated, because, taking for example the paths `RRNRN` and `RNRRN`, if that second respin on the first path gave you enough items to afford a research you couldn't previously,
it's very likely you'll get a different wheel, because it won't be prioritizing the prerequisites for that research anymore.

So, to implement this, you'd probably have to keep state information for how many unaffordable research you have at each step, and note if it changes during a step.
Then, you'd have to have an algorithm that, after each new wheel purchase compares the current state to any other paths that also have a new wheel purchase at the current step.
Then, compare whatever state information is needed.
You'd also have to consider that the default option is to automatically respin if the wheel lands on a x2 or x3, meaning another call to the spinner RNG.

Another consideration: using research itself to manipulate the wheel generator RNG.
The wheel generator rolls the RNG once for each research that can't be afforded, so perhaps buying/resetting research and buying/selling items could be integrated as steps for even more possibilities.

I suppose I'm not sure if this is an exhaustive list of what influences wheel generation, so maybe there are more confounding factors once that one is solved.
That's just the one I know of; it took me quite a while to track down in the initial implementation of this app.

### Parallel processing and saving path progress

Another way to increase processing speed is to do it in parallel.
The game uses a lot of global state, so it would have to be implemented as separate processes, each with their own instance of the solver and the game.

Saving path progress would help this along as well, allowing you to stop and resume path processing and allowing straightforward synchronization of the worker processes.

## 💻 API

If you'd like to use this tool as a library for your own project, go right ahead!
`WheelWrapper` is where a lot of the Infinitode magic happens, so I've tried to thoroughly document it.
`LinearWheelSolver` and `RecursiveWheelSolver` contain the algorithms that tries all the possible choices.
`TheGame` is also worth a mention; that's where the parts of the game that we need are initialized.

`InfinitodeWheelSolver` is the main class that runs the algorithm.
For debugging purposes, there's also `OriginalGameLauncher`, which does the minimum required setup to set the game as modified (to avoid sending crash reports and such) and launch it.
This lets you set breakpoints and debug the real game as it's running.
I used it quite a bit when debugging wheel generation, because you can set breakpoints in the game code, watch for both the original game and this app to hit them, and then compare behavior.
