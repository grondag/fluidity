# Table of Contents
- [Introduction](#introduction)
  - [Purpose](#purpose)
  - [Features](#features)
  - [License](#license)
  - [Status](#status)
  - [Relations](#relations)
    - [Fabric API](#fabric-api)
    - [LibBlockAttributes](#libblockattributes)
    - [Cardinal Components](#cardinal-components)
    - [Vanilla Minecraft](#vanilla-minecraft)
  - [How Fluidity is Organized](#how-fluidity-is-organized)
  - [Dev Environment Setup](#dev-environment-setup)
  - [Examples](#examples)
- [Articles](#articles)
  - [Discrete vs Bulk Articles](#discrete-vs-bulk-articles)
  - [Getting an Article Instance](#getting-an-article-instance)
  - [Article NBT Tags](#article-nbt-tags)
  - [Stored Articles](#stored-articles)
    - [Implementation Support](#implementation-support)
    - [Stored Article Handles](#stored-article-handles)
- [Fractions](#fractions)
  - [Fractions without `Fraction`](#fractions-without-fraction)
- [`Store` and its Variants](#store-and-its-variants)
  - [Querying Store Content](#querying-store-content)
    - [Best Practice: Don't Query Store Contents](#best-practice-dont-query-store-contents)
    - [Querying by Handles](#querying-by-handles)
    - [Query via `forEach`](#query-via-foreach)
    - [Querying Quantities](#querying-quantities)
  - [Store Operations](#store-operations)
    - [ArticleFunction](#articlefunction)
    - [Supplier and Consumer Article Functions](#supplier-and-consumer-article-functions)
  - [Store Event Streams and Storage Listeners](#store-event-streams-and-storage-listeners)
  - [API Variants](#api-variants)
  - [Implementation Variants](#implementation-variants)
- [Transactions](#transactions)
  - [Using Transactions](#using-transactions)
  - [Implementing Transaction Support](#implementing-transaction-support)
  - [Transaction Mechanics](#transaction-mechanics)
- [Device Components](#device-components)
  - [Registering Device Component Types](#registering-device-component-types)
  - [Using Components](#using-components)
  - [Providing Components](#providing-components)
  - [Item Actions](#item-actions)
- [Multiblocks](#multiblocks)
- [Transport](#transport)
  - [What is a Pipe?](#what-is-a-pipe)
  - [Carriers in Brief](#carriers-in-brief)

# Introduction
Fluidity is a game resource storage and transport API for the Fabric tool chain. 

## Purpose
Provide a consistent and comprehensive API for storage and transport of any quantifiable and uniquely identifiable in-game resource, with flexible usage and excellent performance at any scale.

## Features
* Resource-type registry with pre-registered types for Items and Fluids
* Unified interfaces for all types of resources
* High-resolution, large-scale fraction implementation for lossless accounting of fluids and other resources that are continuous instead of discrete, or which have sub-unit quantization
* Registry for discovery and access to specific storage/transport implementations
* Transactions with support for heterogeneous resource types, nesting and concurrency (with limitations)
* Many base classes and helpers to accelerate implementations

## License
Fluidity is licensed under the Apache 2.0 license for easy and unrestricted inclusion or modification in other projects.

## Status
Fluidity is still in active development and breaking changes are likely. The author recommends that usage be restricted to testing and evaluation for now.  All public-facing interfaces and classes are (or should be) annotated with `@API(status = Status.EXPERIMENTAL)`.  These annotations will be updated as the library stabilizes.

Note that issue reports and pull requests are welcome and encouraged.

## Relations

### Fabric API
Fluidity is designed, coded and licensed so that some or all of it could be incorporated into the Fabric API if desired.  However, it is much more extensive than the Fabric project would likely want to take on. And unless or until Fluidity stabilizes and emerges as some sort of *de-facto* community standard there is no particular justification for incorporating even a subset of it into the core Fabric API.

It seems more likely that Fabric API will eventually adopt some less comprehensive set of interfaces that may be influenced by or derived from this and other community efforts, such as LBA (mentioned below). In that case, the author intends to make Fluidity support and extend relevant "official" APIs as they emerge.

### LibBlockAttributes
Fluidity necessarily covers some of the same ground as [LBA](https://github.com/AlexIIL/LibBlockAttributes).  The author intentionally did not closely study LBA while Fluidity was being developed, both in order avoid making a derivative work and to ensure a fresh perspective.  That said, cross-compatibility with LBA is an explicit goal and will be pursued when Fludity stablizes and as time permits.  From admittedly superficial observation, this appears attainable with reasonable effort.

### Cardinal Components
Fludity Device Components (explained below) may be seen to overlap somewhat with [Cardinal Components API](https://github.com/NerdHubMC/Cardinal-Components-API). However, this apparent redundancy is superficial.  

Fluidity Device Component Types focus on discovery and retrieval of implementations and do not provide dynamically extensible data attributes, serialization or other facilities offered by CCA.  Indeed, CCA may prove to be quite complimentary to some Fluidity implementations.    

### Vanilla Minecraft
Fluidity currently has no Mixins and makes no (intentional) changes to vanilla Minecraft behaviors.  Implementations are expected to make their own choices regarding their compatibility with vanilla mechanics.  The poor encapsulation of `Inventory` and it's sub-types is particularly problematic for large-scale storage networks that aim to be performant, and there is no clear best answer for how compatible vs how optimized any given block or item ought to be - it depends on how it will be used.

The library offers some base implementations and helpers to more easily support `Inventory` when that is the desired outcome, along with a way to register handlers for buckets, bottles and similar items.  Even so, vanilla mechanics received cursory attention at best in the initial development and the author considers this an area of opportunity for future improvement. 

## How Fluidity is Organized
The fludity source tree is divided into four packages as follows:
* [**`grondag.fludity.api`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/api)
Public interfaces for use and implementation by mods.  These are the only types an implementation *must* use or be aware of.

* [**`grondag.fludity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base)
Base implementations and helpers.  These are extensive and probably of interest to most implementations but their use is entirely optional.

* [**`grondag.fludity.impl`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/impl)
Internal implementations and helpers.  Mods should not directly reference anything in this sub-tree.

* [**`grondag.fludity.wip`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/wip)
Work-in-process code that will *probably* become part of the library in the near future but is more experimental than even the API Guardian `EXPERIMENTAL` annotation would indicate.  Mods are welcome to look at it, test and provide feedback but should have no expectation of stability. This sub-tree replicates the api/base/impl divisions of the main API to indicate where the code will eventually land.

## Dev Environment Setup
Fluidity requires Fabric API but has no external run-time dependencies beyond those already bundled with Minecraft and Fabric.
 
Add the maven repo where my libraries live to your gradle repositories block:

```gradle
repositories {
  maven {
    name = "dblsaiko"
    url = "https://maven.dblsaiko.net/"
  }
}
```

And then add the following to dependencies:

```gradle
dependencies {
  // optional dev env annotation support
  compileOnly "org.apiguardian:apiguardian-api:1.0.0"
  compileOnly "com.google.code.findbugs:jsr305:3.0.2"

  modImplementation ("grondag:fluidity-mc115:${project.fluidity_version}.+") { transitive = false }
  include "grondag:fluidity-mc115:${project.fluidity_version}.+"
}
```

## Examples
At time of writing, the only mod using Fluidity is [Facility](https://github.com/grondag/facility).  Facility exercises all major features of the API and cloning it into your dev environment is likely to be the best and fastest way to understand how Fluidity can be used in practice. Facility also has an unrestrictive license.

# Articles
An `Article` is a game resource that can be uniquely identified, quantified and serialized. An `ArticleType` defines the class of resource and provides packet and NBT serialization functions.  

Fluidity pre-defines two article types: `ArticleType.ITEM` and `ArticleType.FLUID` to represent in-game items and fluids.  However, any class can be used as an article type via `ArticleType.Builder` and `ArticleTypeRegistry`.  Some possible uses would include non-fluid bulk crafting resources, XP, power systems, or mana.  

Note that no restriction is made against defining new article types that also use `Item` and `Fluid` resources.  However, for compatibility it is recommended that mods adopt the predefined `ITEM` and `FLUID` article types for inter-mod resource storage and transport.

## Discrete vs Bulk Articles
The creator of an article type chooses if the article is *discrete* or *bulk*.  Discrete articles are meant to be counted as individual, atomic units.  Bulk articles are divisible into some unit less than one or fully continuous and thus meant to be measured using fractions. (More on those in a bit.)

However, this distinction is *purely advisory.*  Fluidity is designed so that *any* article type can be measured using either sort of accounting.  Whole numbers, after all, are simply a sub-set of the rational numbers.  The main benefit to using integers over fractions is slightly better performance and memory efficiency.  But if you want to build an "item tank" that stores fractional pick-axes, this is the library for you.

## Getting an Article Instance
Use `Article.of(articleType, resource)` to get a custom article.  `Article.of(item)` and `Article.of(fluid)` are more concise for those common types.  `Article` also exposes static methods for de-serializing any article from an NBT tag or packet buffer.

Retrieving an article is generally non-allocating after the first retrieval because all article instances are interned or cached. This means article instances can be compared using `==` *unless* they contain an NBT tag. (See below)  For this reason, Articles should always be compared using `.equals()` unless the situation absolutely ensures no tags are present on any article being compared.

## Article NBT Tags
An `Article` *may* have an NBT CompoundTag value associated with it.  Currently this functionality is only exposed for `ITEM` articles (because Minecraft requires it) but may be opened up for other article types in a future release.  However, mod authors are *strongly* advised to avoid using tags in favor of simply creating a larger number of distinct resource instances.

When an article has a non-null tag value there can be a virtually infinite number of distinct instances. Interning such articles would create the risk of excessive memory allocation.  Thus, article instances with tag values are held in a fixed-capacity cache and evicted as needed, making them slightly less efficient than articles without tags.

To ensure that articles are immutable, an article's tag instance is not directly exposed. The `copyTag()` method is the only way to get the tag content and results in a new allocation every time.  If you only need to test for tag existence or test for tag equality use `hasTag()` or `doesTagMatch()`.

## Stored Articles
When an article is being stored or transfered we need additional information: quantity and, sometimes, a location. A core design principle of Fluidity is that all such data should never be directly mutated outside of the storage/transport implementation - all changes *must* be the result of some controlled, observable operation.

This is why the API that exposes this information is immutable: `StoredArticleView`.

`StoredArticleView` includes *both* a whole-number `count()` (for discrete articles) and a fractional `amount()` (for bulk articles).  This creates a tiny amount of extra work for implementations (which is largely handled automatically via the base packages) at the benefit of having fewer/simpler interfaces overall.  Consumers of the API can use whichever accounting method makes sense for their purpose.  

Note the `count()` property will not include fractional amounts and so is not a reliable test of emptiness for implementations that may contain bulk items.  To test for emptiness, use `isEmpty()`.

`StoredArticleView` has a special instance meant for use in place of `null` values: `StoredArticleView.EMPTY`.  Implementations should return this instance instead of `null` when the intent is to signal the absence of a result.

### Implementation Support
Obviously, implementations *will* need to mutate their contents and most implementations will be firmly discrete or bulk - not both.  The [`grondag.fluidity.base.article`]() package provides specialized discrete/bulk interfaces and classes to support most types of implementations.  Use of these is entirely optional but mod authors are encouraged to examine them for illustration before creating their own.  

### Stored Article Handles
`StoredArticleView` exposes an integer `handle()` property, which is *similar* in purpose to vanilla inventory "slots" but also different in key ways:

* Handles are not guaranteed to correspond to a specific, "physical" location in storage. Some implementations (something like a Storage Drawers mod, for example) may have this contract, but it is never required.

* Handle can be used to retrieve a storage view (similar to `List.get()`) but the targets of storage transactions are *always* specified by article - never by handle.  This ensures that no transaction is ambiguously or erroneously specified. A vanilla `Inventory` will let you blindly replace or change the contents of a slot without knowing or validating what was in it. Fluidity *never* allows this in its public API. Implementations that extend this to allow transactions based on handle (again, something like Storage Drawers would require this) are advised to *also* include article as part of any transaction specification. (The `FixedDiscreteStorage` interface in `grondag.fluidity.base.storage.discrete` and its sub-types offer an example of this.)

* Storage implementations are required to maintain a consistent handle:article mapping for as long as the storage has any listeners. In practice, this means preserving empty handles and creating new ones when articles are completely removed and new articles are added.  This makes is much easier for listeners to maintain a synchronized view of content and/or respond appropriately to changes.  Except...

* Implementations that *do* have physical slots *may* change a handle:article mapping, but when doing so must send listeners two events: one to remove the article from its current handle association (the listener would associate the old handle with `StoredArticleView.EMPTY`) and a second event to re-add the article with its new handle.  Implementations that have other reasons to change handle:article mappings may also do so if they follow the same practice.

# Fractions
Fluidity represents Fractions as three `long` values: whole units, numerator and denominator.  While two longs (numerator and denominator) would arguably be sufficient for most use cases, that arrangement would cause scale and sub-unit resolution to vary inversely.  In an unpredictable multi-mod environment, it is better for mod authors (and mod testers) if the maximum resolution and scale of fractions are both invariant.

Fluidity includes two concrete implementations as part of the public API: `Fraction` and `MutableFraction`. These are and do what one would expect based on the their names. Implementations that frequently update fractional values will generally want to use `MutableFraction` instead of allocating new `Fraction` instances with every operation.

For this reason, consumers of `Fraction` should *never* retain a reference but instead copy the value to a `MutableFraction` instance, or use `Fraction.toImmutable()` to ensure you have a Fraction instance that is safe to hold.

These classes were designed to be final in order to ensure static invocation in what are likely to be frequent calls and mod authors should not try to sub-class or modify them via reflection, Mixins or other esoteric methods. If you really need them to do something else, please submit a PR.

## Fractions without `Fraction`
In many Fluidity methods, fractional quantities are exposed and manipulated as primitive values, without use of `Fraction` itself.  These methods (see below and in the code) instead rely on `long` values that correspond to `Fraction.whole()` or `Fraction.numerator()` and `Fraction.denominator()`.  When available and appropriate for your use case (i.e. when you have fixed minimum and maximum transfer amounts) method with primitive arguments will generally be slightly more performant and easier to work with. 

# `Store` and its Variants
A `Store` in Fluidity is an instance that holds and, optionally, supplies or accepts quantified articles. Stores may also publish an event stream useful for synchronizing store contents to client-side GUIs or implementing aggregate views of multiple stores.

A store may contain any `ArticleType` or combination of article types, using either discrete or bulk accounting and the interfaces are designed so that all implementations *must* support queries and operations using either discrete or bulk (fractional) quantities. This allows consumers of any storage implementation to have rigid and simple code paths, and helps to limit redundancy and the size of the API more generally.

In practice, most implementations are likely to store one article type (or a small, focused set of article types) and will use the form of accounting (discrete or fractional) most appropriate for that set.  Fluidity includes base classes and helpers to make this flexibility relatively easy to attain - mod authors should only need to handle the use cases that matter for their implementation.

## Querying Store Content
Unlike `Inventory` a `Store` is never asked to expose its internal representation of content, or even all of its content. If you ask a store a question it will give you the answer it wants you to have. A store should not lie, but it doesn't have to tell the whole truth.  Consumers should expect store query results to be consistent, but shouldn't try to infer information the store hasn't explicitly provided.

### Best Practice: Don't Query Store Contents
The rest of this section will describe the various ways you can get information about a store, but most of the time you should not. If a store reports it has an article, that does *not* imply the store will supply a certain amount of the article if asked.  If a store is empty, that does *not* mean it can accept a particular article in whatever amount.

The *only* reliable way to know if a store can accept or supply some quantity of an article is to try it - simulating if the intent is informational.  This is a very deliberate limitation in the contract of the `Store` interface, and is meant to ensure that `Store` implementations have flexibility in how they operate.  Any attempt to codify the rules what a chest or tank might be allowed to do would have to anticipate every possible use case (and thus be very extensive). And it would almost certainly fail in this attempt, eventually becoming an obstacle to somebody's cool idea.

Another expected use of store queries might be to synchronize information to clients for display, or to aggregate contents from multiple stores into a single view.  In both cases, subscribing to the stores' event streams (described below) will be more reliable and performant.

The best (and intended) use of the query mechanism explained below is to emulate `Inventory` behaviors, or gather summary information for *ad-hoc* display or debugging, especially when working with stores from different mods. They also serve as a back-stop to handle use cases that were not otherwise anticipated. 

### Querying by Handles
As described earlier, a `StoredArticleView` exposes a `handle()` property, and iterating a store's contents via handle or retrieving the article view for a specific known handle will usually be the most straightforward way to navigate store content in those rare cases when it is necessary. The relevant members are `handleCount()` and `view(handle)`.

Stores are generally not meant to be thread-safe and consumers should check `handleCount()` or `isHandleValid()` before calling `view(handle)` but `Store` implementations should return `StoredArticleView.EMPTY` when an invalid handle is encountered.  This is a functionally correct result, and simplifies store implementation and usage overall.

### Query via `forEach`
`Store` also exposes a pair of specialized `forEach()` methods.  These methods have naive default implementation that simply iterate using handles, but should be overridden by `Store` implementations where iteration by handle may be expensive. Unlike a standard `forEach` method, these accept a `Predicate` as the action, and iteration will stop if the predicate returns `false.` This behavior is especially useful when you only need to act on the first article. An alternate version of `forEach()` accepts a second predicate the `Store` will use to filter articles before applying the action.  This simplifies the action predicate, and may be faster for some implementations.

### Querying Quantities
Sometimes, you just need to know if a store has a particular article in stock.  The `countOf()` and `amountOf()` methods offer this ability. But note again: the only reliable way to know if a store can actually supply an article is to request it and observe what happens.  This is exactly how these methods work in their default implementation - they simulate maximum extraction of the requested article and return the result.

## Store Operations
While a store may receive requests for any article type with either discrete or fractional quantities, a store can reject or partially fulfill any request that isn't supported,  simply by returning zero or a lesser amount than what was requested. 

For example, a fluid tank could be implemented using discrete accounting to store only whole buckets.  Such a tank would return empty results for any request to fill or drain any sub-unit amount, and would only partially fulfill requests for one or more units that also include a fractional amount.  A tank could also be designed to only accept a single type of fluid, and thus reject any request to drain or fill other fluids (or other types of articles).  More generally, implementations can adopt any constraint that doesn't violate the contract of exposed interfaces, and those interfaces were designed to allow flexibility.

### ArticleFunction
Storage input and output operations use the same interface: `ArticleFunction`.  

`ArticleFunction` is overloaded, but every variant accepts an `Article` (or something that can be converted to an `Article`, like an `Item`) and a `simulate` parameter.  When the `simulate` parameter is true, the result of the operation will be a forecast only and the state of the `Store` will not change.

The remaining variation is in how quantities are specified, as follows:

* A single `long` value - for discrete articles or when only whole (bucket) units are needed.  The result will also be a whole unit.

* Two `long` values - a numerator and denominator - for fractional accounting using primitives.  The result will a  multiple of the denominator.

* A Fraction instance - for full-scale, full-precision fractional accounting. The result will be a Fraction.

All `ArticleFunction` implementations *must* accept all of these variants, but as with `Store` most implementations will adopt a specific form of accounting internally and rely on the base classes provided by Fluidity to convert mismatched requests to an appropriate form.  

Note there are some subtle differences in how the quantity input affect the outputs: variations that accept `long` values must also return results that can be expressed as `long` values.  This means, for example, that a request to supply 2/3 from a tank that is 1/2 full can only return 1/3 if the result must be expressed as two long values. But if the same request is made with 2/3 (as a `Fraction`) then the result will be 1/2 (also as a `Fraction`).

The lesson here is this: if you can accept Fraction values then you should also make requests using Fraction values.  If you can't accept Fraction values, then primitive values are exactly what you need, because that will ensure you never get a result you can't accept.  In either case you should not assume an empty result means a store is empty or full - it only means the store can't supply or accept the amount requested in the form you requested it.   (Use `Store.isEmpty()` or `Store.isFull()` to answer those questions.)

### Supplier and Consumer Article Functions
The quantity parameters to `ArticleFunction`, in any form, are *always* zero or positive.  (Fraction denominators must be >= 1) The direction of articles in or out of storage is instead implied by which article function is called: 

* **`Store.getSupplier()`** Use to remove articles from a store. (Or to forecast the result of a removal request,)

* **`Store.getConsumer()`** Use to add articles to a store. (Or to forecast the result of an add request.)

Both methods by default return `ArticleFunction.ALWAYS_RETURN_ZERO` - a special implementation of `ArticleFunction` that, unsurprisingly, always returns zero in response to any request. A store can be made insert-only or extract-only by overriding only one of these methods.

Implementations should not override these methods to return `null`. The default return value can be used as-is with the (correct) result that no operation will change state.  Classes that do frequent operations on many stores may see some performance benefit from excluding inoperable stores by testing for the presence of a real implementation using the convenience methods `hasConsumer()` and `hasSupplier()`. 
 
## Store Event Streams and Storage Listeners
A store can optionally expose a `StorageEventStream` (via `Store.storageEventStream()`) to broadcast changes to storage state to interested listeners.  Listeners must implement `StorageListener` and pass themselves to `StorageEventStream.startListening()` to begin receiving events.

The `StorageListener` interface, like others in Fluidity, must handle both discrete and fractional accounting.  To this end, there are two versions of events, one with a single long quantity and one with a fraction.  Here as elsewhere, Fluidity includes base classes to reduce the burden on implementations.

As with `ArticleFunction`, quantity parameters are always positive.  Direction is shown by which method is called: `onAccept()` for articles coming into the store, and `onSupply()` for articles going out.  These methods include both the amount changed and the new amount that results.  This bit of redundancy is easy for the store to provide (it must have this information anyway) and makes many listener implementations simpler if they have no need to track prior state. It can also make it easier to isolate state synchronization errors.   

Stores should not notify listeners of simulated requests. It would serve no useful purpose and there is no simulation flag in any of the notification methods.

Stores that are enrolled in a transaction (explained in a later section) should generally send notifications immediately and not wait for the transaction to be committed. Some listeners may depend on the store's state and could also be enrolled in the same transaction.  If such listeners are not notified of events as they happen, they could give invalid results.  This means that if a transaction is rolled back, the store must send events that reverse earlier notifications.  Again, the quantities are always positive - an `onAccpt()` notification is reversed by a matching `onSupply()` notification, etc.      

A store should respond to a new listener by immediately sending acceptance notifications for current content that should be visible to that listener.  This avoids the need for any listener to know how to query the store to build an initial state vector that will be exactly synchronized with the event stream. This behavior can be disabled when unneeded via the `sendNotifications` parameter to `startListening()`.  

The same pattern applies to the `stopListening()` method - the store should respond by sending supply notifications for all current content unless asked not to.  This behavior is particularly useful for aggregate views of multiple stores because store addition and removal can be handled by the same routines that handle accept and supply events.

Stores that do not implement `Store.eventStream()` should rely on the default implementation to return `StorageEventStream.UNSUPPORTED`. Consumers of event streams should check for the presence of a valid instance using `hasEventStream()` before subscribing. Subscribing to `StorageEventStream.UNSUPPORTED` logs a one-time warning as an aid to troubleshooting but no exception is thrown and no event notifications will be received. 

## API Variants
The API currently includes two variations on 'Store`:
* **`FixedStore`** For stores with fixed handles, adds handles to operations via `FixedArticleFunction` extension of `ArticleFunction` - for bins, drawers, or vanilla-like storage with fixed "slots"
* **`InventoryStore`**  Combo of `Store`, `RecipeInputProvider` and `Inventory` with a few default handlers - useful mainly as consistent shorthand for this combo

## Implementation Variants
[**`grondag.fluidity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base/storage) and its sub-packages include the following interfaces and classes to facilitate various types of `Store` implementations:

* **`DiscreteStore` and `FixedDiscreteStore`**  Extensions of `Store` and `FixedStore` with default implementations for fractional accounting - implement these when tracking whole units
* **`BulkStore` and `FixedBulkStore`**  Extensions of `Store` and `FixedStore` with default implementations for discrete accounting - implement these when tracking fractional units
* **`AbstractStore`**  Has core components of listener notifications
    * **`AbstractAggregateStore`**  Handles tracking of multiple stores
        * **`AggregateBulkStore`**  Tracks combined view of content from multiple stores using fractional accounting
        * **`AggregateDiscreteStore`**  Tracks combined view of content from multiple stores using discrete accounting
    * **`AbstractLazyRollbackStore`**  Adds single-store hooks for transaction participation and rollback
        * **`AbstractDiscreteStore`**  Base class for simple stores using discrete accounting 
            * **`CreativeBinStorage`**  What it sounds like - fixed handles and creative behaviors
            * **`DividedDiscreteStore`**  Non-`Inventory` store with fixed handles and per-handle capacity limits, implements `FixedStore`
            * **`FlexibleDiscreteStore`**  Non-`Inventory` store with dynamic handles and store-level capacity limit
            * **`SlottedInventoryStore`**  Fixed-handle, fix-slot store, implements `InventoryStore`
        * **`SimpleTank`** Single-article bulk store with volume limit
        * **`SingleStackInventory`** `InventoryStore` implementation wrapping a single `ItemStack`, prototype for storage items
* **`ForwardingStore`**  Wraps a `Store` instance - override methods as needed to modify behavior of an existing store 

# Transactions
Transactions allow a `Store` or any class that implements `TransactionParticipant` to be notified when an operation is part of a transaction and may need to be undone.  Participants are then given an opportunity to save any necessary rollback state before the operation happens, and then notified when the transaction is committed or rolled back.

Participants can be explicitly enlisted in a transaction when their involvement is known, but implementations can also self-enlist.  This is particularly useful for transportation networks with cost accounting - the initiator of an operation may not know all of the attendant costs and side effects of the transport network, or even that they exist.  If the transport network can self-enlist, all of that can be handled without complicating the initiating code. 

Transactions are useful in at least two ways:
1. Operations across multiple participants do not have to forecast results with perfect accuracy, nor handle clean up of participant state when things do not go according to plan.  When working with complicated implementations (something like a Project E table, for example) both the forecasting and the cleanup could be nigh-impossible to get right and will inevitably result in undesirable coupling of implementations.
2. Code that initiates an operation does not have to know of and handle all of the possible side effects that could result because transaction participants that aren't directly known or meaningful to the initiator can self-enlist.

## Using Transactions
The transaction-related interfaces are located in `grondag.fluidity.api.transact`.
* **`Transaction`**  A single transaction - may be nested within another transaction. The initiator obtains this instance and uses it to commit or roll back the transaction, and to enlist participants.  Should be enclosed in a try-with-resources block - default close behavior is to roll back unless `commit()` was called successfully before `close()`.
* **`TransactionParticipant`**  Provides a `TransactionDelegate` and indicates if the participant is self-enlisting. Implement this on stores, transport carriers, machines or other game objects that can benefit from transaction handling. All Fluidity base implementations (except aggregate views) include transaction support.
	* **`TransactionDelegate`** Does the actual rollback preparation and handles closure notifications. Allows participants to share the same rollback state.  The `ArticleFunction` interface itself extends `TransactionParticipant` so it is common to have multiple `ArticleFunctions` instances that internally update the same state.
* **`TransactionContext`**  Exposed to participants at time of enlistment, and again at close.  Used to save and retrieve rollback state, and to query commit/rollback status at close.

Here's an example of a simple transaction reliably transferring one unit of something between two stores:

```java
try(Transaction tx = Transaction.open()) {
	tx.enlist(firstStore);
	tx.enlist(secondStore);

	if (firstStore.getSupplier().apply(myArticle), 1) == 1 && secondStore.getConsumer().apply(myArticle, 1) == 1) {
		tx.commit();
	} else {
		tx.rollback();
	}
}
```

As we'll see in the Transport section we may only have an `ArticleFunction` to work with instead of a full-fledged `Store` instance.  That's why `ArticleFunction` also extends `TransctionParticipant` - you don't have to know where a supplier or consumer function came from to enlist it in a transaction.

## Implementing Transaction Support
The Fluidity base implementations include several different variations of transaction support that take advantage of specific implementation characteristics.  The reader can look to those as examples. In particular, see `AbstractLazyRollbackStore` and it's sub-types.

The main principle to follow is to defer creating rollback state until something actually changes, unless creating rollback state is very inexpensive.  Often, the easiest way to accomplish this is to make `TransctionParticipant.isSelfEnlisting()` return true, and then call `Transaction.current.enlistSelf()` right before something changes.

Transactions track which delegates have already been enlisted (self-enlisted or otherwise), and guarantees that `TransactionDelegate.prepareRollback()` will be called exactly once, immediately when the delegate first becomes enlisted.

The rollback state provided by the delegate via `TransactionContext.setState()` can be any Object, or null.  It will never be inspected or altered by the transaction manager, and will be provided back to the delegate via `TransactionContext.getState()` when the transaction is closed.

The `Consumer` function returned by `prepareRollback()` will *always* be called, both when a transaction is committed and when it is rolled back deliberately or due to an exception.  Implementations *must* therefore check the value of `TransactionContext.isCommited()` to know what action is appropriate.

Oddball implementations that don't need to do anything on commit or rollback can return `TransactionDelegate.IGNORE` as their delegate, which does exactly what you'd expect it to do.  Examples of this are creative-type storage or void blocks where state is essentially immutable, and aggregate storage implementations that don't have any internal, independent state that would need to be restored but instead rely on their component instances to handle transaction state and change notifications as needed. 

However, a `Store` or other class that extends `TrasactionDelegate` *must* provide transaction support to the extent that it means anything for that implementation.  Transaction delegates should not be null and they should not be `TransactionDelegate.IGNORE` unless that gives "correct" results. 

See also the related notes regarding storage event streams and listeners, above.

## Transaction Mechanics
Fluidity Transaction State is global state.  There is only ever one current transaction, across all threads.

As we all know, global state is always bad.  Sometimes, it is also *least* bad. Nobody wants to wear the cleanest dirty shirt, and being forced to do so may motivate us to improve the regularity of our laundering habits, but sometimes we must choose from a menu of unsavory options. The author believes such is the case here.

Earlier designs considered the possibility of partitioning transaction state into isolated scopes, and to allow concurrent transactions from multiple threads, much as a modern RDBMS would do.  However this immediately introduces many complicating problems, including the need to track which objects are visible in which scope(s) and the need for synchronization when such objects are referenced in more than one scope. It also creates the need to detect and handle deadlocks, or otherwise shape the API in a (probably onerous and restrictive) way so that deadlocks cannot occur. It's simply not worth it in the context of modded Minecraft, assuming it could be made to work at all before we have all moved on to other pursuits. 

When there can only be a single current transaction, transaction state is *de-facto* global state.  *Exposing* it as explicit global state is not essential, but it ends up being *very* nice for allowing transaction participants to self-enlist.  This single change greatly simplified implementations that benefit from lazy rollback preparation or want to automatically include side-effects (like transport costs) in the transaction that caused the side effects to occur. Doing this without global visibility requires cluttering the API to pass the current transaction up and down the call stack, or exposing it on some accessible instance such that it becomes effectively global anyway.

Fluidity also supports nested transactions, and making the overall transaction state a singleton allows the nesting implementation to be a simple stack of zero or more transactions associated with a single thread, which brings us to the question of support for multiple threads. 

Fluidity *does* support transactions initiated from any server-side thread, and automatically synchronizes the current state via a specialized locking mechanism. This mechanism makes the following guarantees:
* Only one thread can own the current transaction state.
* To open a new transaction, there must be no current transaction, or the calling thread must already own the current state.
* A thread that tries to open a transaction without holding the lock will block until the transaction is complete. This will not cause deadlocks unless an action somehow waits on a blocked thread through some dependency other than the transaction.  That would be strange and bad. Don't do that.
* Most important: the Minecraft server thread is *always* given priority above all other threads. If a transaction from another thread is open when the server thread tries to open a transaction, the server thread will block until that thread completes, and will then be scheduled before any other waiting threads.  Non-server threads should be scheduled in an approximately fair order. 

The rational for this last guarantee is performance: the server thread should not be held up by locks from other threads.  That said, the server thread will not be using the transaction state most of the time - server ticks only happen 20 times per second and, ideally, are short.  A future update may further restrict locks from other threads, only allowing them to proceed outside of the server tick event.

That all said, the best practice for opening transactions from other threads is: don't.  It makes everything much more complicated and prone to breakage.

If, like the author, you have some mods that *really* must move some work off the server thread to avoid killing it, the answer is *still* don't initiate transactions from outside the server thread.  If you are querying or changing any state you don't completely control, it probably doesn't expect to be queried or changed from anywhere other than the server thread, preferably during server tick. 

A better approach is to completely isolate the state that will be processed off-thread, buffering world state if needed, and then synchronizing with world state during each server tick.  During the server tick you can initiate transactions on the server thread, and those transactions can consume or produce state that is the result of or input to off-thread processing.  Server ticks may take a little longer to run, but you won't block them or break the game (probably), and usually there is time to spare or to be found with server-side optimization mods.  

Fermion Simulator and Working Scheduler both provide some mechanisms that could be useful for this sort of setup, and if you are committed to doing concurrent processing server-side it should be something you are comfortable doing on your own if needed.

Consistent with this recommendation, there is a non-zero chance support for transactions from non-server threads will be altogether *removed* in some future release and this is a topic on which the author would value feedback.  For now the feature remains to account for scenarios that may not have been anticipated, and because someone will probably try to do it anyway.

Lastly, note that transactions are a server-side construct, and no client-side code should ever reference them.  This is difficult to enforce directly without expensive checks, and so for now mod authors are on the honor system to get this right.  If it becomes a problem, some checks may be added, perhaps with configuration to turn them on or off.

# Device Components
A *device* in Fluidity is a functional role - there is no `Device` interface or class.  "Devices" are game objects (currently blocks and items) associated with one or more "Device Component" instances. Device components are also conceptual and can be of any type - there is no `DeviceComponent` interface.

Many readers will note some resemblance here to entity attribute frameworks like Cardinal Components or the old capabilities system in Forge.  The resemblance is superficial: Fluidity facilitates *access* to device components but handles no aspects of implementation or persistence, except that some of the interfaces it defines are meant to be exposed as device components and it also provides (optional) base classes that can be used to implement those interfaces.  

Fluidity is in no way a general-purpose entity attribute library, nor is it meant to replace one.  The intent here is to decouple component access from component implementation, and to do so without adding or requiring external dependences.  We also introduce some rudiments of device access control, as will be explained below. 

## Registering Device Component Types
Creating a new component type is straightforward, as shown by the code Fluidity uses to create components for storage access:

```java
DeviceComponentType<Store> STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "storage"), EMPTY);
```

Note that two components can share the same type:
```java
	/**
	 * Multiblock storage devices may elect to return the compound storage instance as the main storage service.
	 * This method offers an unambiguous way to reference the internal storage of the device.
	 *
	 * <p>Also used by and necessary for aggregate storage implementations for the same reason.
	 *
	 * @return Internal {@link Store} of this device, or the regular storage if not a multiblock.
	 */
	DeviceComponentType<Store> INTERNAL_STORAGE_COMPONENT = DeviceComponentRegistry.INSTANCE.createComponent(new Identifier(Fluidity.MOD_ID, "internal_storage"), EMPTY);
```

The second parameter to `DeviceComponentRegistry.createComponent()` is the value that should be returned when the component is absent, and visible via `DeviceComponentType.absent()`.  You can use `null` here, but cleaner code may result when the absent value is a no-effect dummy instance.  This is a choice for the component implementation to make.   

## Using Components
Obtaining a device component instance is a two-step process:

1) Call a variation of `DeviceComponentType.getAccess()` to retrieve a `DeviceComponentAccess` instance. Currently there are methods to get access from blocks (or block positions within a world) and from Items, which may or may not be held by a player.  Future versions may add access methods for entities or other game objects if there are interesting use cases for them. 

2) Use a variant of `DeviceComponentType.get()` to get the actual component instance, or the absent value if the component is unavailable.

`DeviceComponentType.get()` accepts three parameters, all of which can be omitted: 

* **`Authorization`**  A placeholder interface for now, represents some sort of access token to be defined in a later version.  Defaults to `Authorization.PUBLIC`

* **`Direction`** For device components that are accessible via a specific side.  Pass `null` (or call a `get()` variant without this argument) for device components that have no side or to get the non-specific instance.  Component types that do not have sides should ignore this parameter and access attempts from a specific side should always provide it, unless it is somehow known to be unnecessary.

* **`Identifier`** Device components may optionally have named instances and accept access requests for a specific named instance.  This may be useful, for example, with machines that have more than one input/output buffer that may be accessible from the same side.  Such a machine could return a different view of storage depending on which named buffer was requested.  Use of this feature is optional and implementation-specific; Fluidity currently makes no attempt to standardize these identifiers or their meanings. 


Often, access to a device component is for a single use.  In these cases, `DeviceComponentType.acceptIfPresent()` can simplify code. It works like `get()` but also accepts a `Consumer` for the component type and if it finds a non-absent component instance it applies the consumer and returns true. It returns false when the component is absent.

Similar convenience is offered by `DeviceComponentType.applyIfPresent()`. It accepts a `Function` over the component type, and returns the result of that function if a non-absent component instance is found, or `null` otherwise.

## Providing Components
Fluidity can only return component instances that have been mapped via `DeviceComponentType.registerProvider()`, like so:

```java
Store.STORAGE_COMPONENT.registerProvider(ctx -> ((TankBlockEntity) ctx.blockEntity()).getEffectiveStorage(), TANK);
Store.INTERNAL_STORAGE_COMPONENT.registerProvider(ctx -> ((TankBlockEntity) ctx.blockEntity()).getInternalStorage(), TANK);
```

While access to components requires two steps, provisioning is handled by a single function.  All of the information about the world/block/item/player/authorization/side/id is marshaled into a `BlockComponentContext` or `ItemComponentContext` instance that the function consumes.

Most block implementations will use a BlockEntity as their component holder/provider, but this is not required.  The provider function must map the context data to a component instance. How that happens is unspecified. The `BlockEntity` value in the context can be `null`, but `World` and `BlockPos` values will always be present.

## Item Actions
Fluidity includes an opt-in system for adding behaviors to interactive items like buckets and bottles.  These items are often expected to perform some action when used on blocks that contain or are associated with a device component.  They also happen to have behaviors pre-defined by vanilla Minecraft and potentially by other mods.  This makes a single, standard and centralized handling mechanism for these behaviors impractical.  At the same time, adding special-case handling for every bottle/bucket/whatever combination to each block is also impractical.

`DeviceComponentType.registerAction` associates a potential action with a device component type and one or more items.  The first argument is a `BiPredicate` that accepts an `ItemComponentContext` and a device component instance.  The intent is that multiple consumers can be registered for the same item/component, and processing will stop after any consumer returns true. Order of execution is unspecified, but this should not matter much in practice - the player can only be holding one item and clicking on one block at a time.

Construction the `BiPredicate` action is not difficult, but does typically involve enough repetitive code to be annoying.  `ItemActionHelper` provides utility methods for quickly registering actions involving Fluid storage and potion bottles or other held items.   They are used like so:

```java
ItemActionHelper.addPotionActions(Fluids.WATER, Potions.WATER);
ItemActionHelper.addItemActions(Fluids.WATER, Items.BUCKET, Items.WATER_BUCKET);
ItemActionHelper.addItemActions(Fluids.LAVA, Items.BUCKET, Items.LAVA_BUCKET);
```

Note from this example that Fluidity pre-defines items actions for the Storage component that can be activated when the `Store` contains water or lava and the item is a bucket or vanilla bottle.  Actions for all other combinations of item type and fluid will need to be defined by mods.

Lastly, note that these actions have no effect unless you invoke Fluidity's action handler in your block's `onUse` method (or wherever is appropriate) using `DeviceComponentType.applyActions` or `DeviceComponentType.applyActionsWithHeld`. This should only be done server-side. For example:

```java
@Override
public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
	final ItemStack stack = player.getStackInHand(hand);

	if(Block.getBlockFromItem(stack.getItem()) instanceof TankBlock) {
		return ActionResult.PASS;
	}

	if (!world.isClient) {
		final BlockEntity be = world.getBlockEntity(pos);

		if(be instanceof TankBlockEntity) {
			final TankBlockEntity tankBe = (TankBlockEntity) be;

			if(Store.STORAGE_COMPONENT.applyActionsWithHeld(tankBe.getEffectiveStorage(), (ServerPlayerEntity)player)) {
				return ActionResult.SUCCESS;
			} else {
				// alternate handling would go here
			}
		}
	}
	
	// might instead be SUCCESS depending on desired behavior
	return ActionResult.PASS;
}
```
 
# Multiblocks
Transport mods often have multi-block constructs - usually cables, pipes or conduits.  Storage mods, too, sometimes have multi-block structures.

The [`grondag.fludity.api.multiblock`](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/api/multiblock) package contains three interfaces to accelerate multi-block device implementations:

* **`MultiBlockMember`** Implement and retain an instance associated with a block position, typically in a `BlockEntity`. The base implementation `AbstractBlockEntityMember` will do most of the work for you.

* **`MultiBlock`** Implement to contain the state and behaviors of the compound structure. Automatically receives notifications when a member is added or removed, and when the structure is destroyed. The `AbstractMultiBlock` class in the `base` package and its sub-types should serve as an adequate base for most implementations.

* **`MultiBlockManager`** Receives game events and forwards them as appropriate to affected `MultiBlock` and `MultiBlockMember` instances, creating or removing multibock instances as needed. Also tracks the existence of all extant multiblocks and their members in every server-side world.  The implementation is included and opaque. Create and retain instances as `static final` during block registration via `MultiBlockManager.create()`.  

`MultiBlockManager.create()` requires only two parameters: a supplier to create new `MultiBlock` instances and a `BiPredicate` to test if two adjacent `MultiBlockMember` instances should connect. 

All multi-block classes are server-side only and should *only* be referenced from the server thread.  The rules for multi-block structure tracking rely on the fact that blocks can only be added or removed one at time, and thus there are only a small number of use cases the `MultiBlockManager` implementation must handle.  

Because the logic is relatively simple and member locations and instances are cached in the manager itself, event handling should be performant even with large-scale structures.

The multi-block manager does not check for the loaded or unloaded status of worlds or chunks - it generally does not interact with the world directly but instead acts through member instances.  Implementations should handle these checks (or make other arrangements) in their own logic.

To use this API, implement the `MultiBlock` and `MultiBlockMember` interfaces (typically by extending one of the provided base classes) and add the behaviors you want to happen when members are added or removed from a multi-block.  Then create a manager for those implementations via `MutliBlockManager.create()`.  Here is an example:

```java
public class TankMultiBlock extends AbstractStorageMultiBlock<TankMultiBlock.Member, TankMultiBlock> {
	public TankMultiBlock() {
		super(new AggregateBulkStore());
	}

	protected static class Member extends AbstractBlockEntityMember<Member, TankMultiBlock, Store, TankBlockEntity> {
		public Member(TankBlockEntity blockEntity, Function<TankBlockEntity, Store> componentFunction) {
			super(blockEntity, componentFunction);
		}

		@Override
		protected void beforeOwnerRemoval() {
			blockEntity.wrapper.setWrapped(blockEntity.getInternalStorage());
		}

		@Override
		protected void afterOwnerAddition() {
			blockEntity.wrapper.setWrapped(owner.storage);
		}

		protected int species() {
			return blockEntity.getCachedState().get(SpeciesProperty.SPECIES);
		}

		protected boolean canConnect(Member other) {
			return other != null && blockEntity.hasWorld() && other.blockEntity.hasWorld() && species() == other.species();
		}
	}

	protected static final MultiBlockManager<Member, TankMultiBlock, Store> DEVICE_MANAGER = MultiBlockManager.create(
			TankMultiBlock::new, (Member a, Member b) -> a != null && a.canConnect(b));
}
``` 
 
The remaining task is to add reliable logic that calls `MultiBlockManager.connect()` and `MultiBlockManager.disconnect()` on the `MultiBlockManager` instance created during block registration. The methods must be called reliably when member instances are added or removed from a world.  Typically the easiest way to do this is via handlers in `BlockEntity.setWorld()`, `BlockEntity.markRemoved()`, and `BlockEntity.cancelRemoval()`.

The implementation is not difficult but the available examples are not compact. For one example, look [here](https://github.com/grondag/facility/blob/master/src/main/java/grondag/facility/storage/StorageBlockEntity.java).  

# Transport
Storage by itself is useful, but eventually the player needs to move stuff from one place to another.  Fluidity will include APIs and base classes to accelerate the development and ensure compatibility of pipes, conduits, conveyers, cables, wires, wireless networks, etc.  The "Transport" category encompasses all of these.

Note: transport features are work-in-progress and liable to change dramatically.  All code for transport is currently in the `grondag.fluidity.wip` package.

## What is a Pipe?
There are at least two concepts of a "pipe," and only one of them requires a transport API.

* **Pipes as Storage:** It's intuitive to model a pipe as just another `Store` with built-in logic to pull or push content from neighboring blocks.  This models real-world behaviors and the logic can be made to depend only on local state and the state of directly-adjacent blocks. For these sorts of implementations, the existing capabilities in Fluidity may be enough.

* **Pipes as Carriers:** When every pipe is also a store, every pipe generally has to process every tick - scale can be a limitation. Special case logic may creep in to dampen undesirable oscillation or cycles. The developer also has to decide if content in a broken pipe enters the world - with all of the attendant complications - or is simply lost.  An alternative is to model pipes as packet carriers in a network, and that is the approach taken in the API presented here.

## Carriers in Brief
The `Carrier` interface models a transport mechanism that can carry traffic between nodes that are connected to it.  Usually a `Carrier` will be a block or multi-block made to appear like a cable, pipe, etc. but could be a wireless network, cargo drones, or represent internal connectivity within a device. 

A device that connects to a carrier must implement `CarrierConnector` and then pass that instance to a `CarrierProvider` that controls access to available carriers.  If the connection is successful, the result will be a `CarrierSession` - a private, privileged view of the carrier node associated with that connection.  Other nodes on the network will see the node (and will themselves be visible as) `CarrierNode` instances.

Every `Carrier` has a single, associated `CarrierType` instance that defines what article types it can carry.  'CarrierType` can optionally include a white-list and black-list of allowed articles.  There are no limitations on the types or combination of articles a carrier can transport.

`CarrierProvider` is a device component, and provides discovery and access to all carriers of any type available from a device. This means a connector does not require knowledge of specific carrier type. It can inspect the types available via `CarrierProvider.carrierTypes()` or use `getBestCarrier(ArticleType)` to have the provider pick a carrier appropriate for the content to be sent or received.

As noted earlier, the transport API is in active development. Documentation is sparse. For deeper understanding, review usage of these interfaces and related classes in  [Facility](https://github.com/grondag/facility).
