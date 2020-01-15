# Introduction
Fluidity is a game resource storage and transport API for the Fabric toolchain. 

## Purpose
Provide a consistent and comprehensive API for storage and transport of any quantifiable and uniquely identifiable in-game resource, with excellent performance and flexiblity of scale and usage.

## Features
* Resource-type registry with pre-registered types for Items and Fluids
* Unified interfaces for all types of resources
* High-resolution, large-scale fraction implementation for lossless accounting of fluids and other resources that are continuous instead of discrete, or which have sub-unit quantization
* Registry for discovery and access to specific storage/transport implementations
* Transactions with support for heterogeneous resource types, nesting and concurrency (with limitations)
* Many base classes and helpers to support implementations

## License
Fluidity is licensed under the Apache 2.0 license for easy and unrestricted inclusion or modification in other projects.

## Status
Fluidity is still in active development and breaking changes are likely. The author recommends that usage be restricted to testing and evaluation for now.  All public-facing interfaces and classes are (or should be) annotated with `@API(status = Status.EXPERIMENTAL)`.  These annotations will be updated as the library stabilizes.

Note that issue reports and pull requests are welcome and encouraged.

## Relations

### Fabric API
Fluidity is designed, coded and licensed so that some or all of it could be incorporated into the Fabric API if wanted.  However, it is much more extensive than the Fabric project would likely want to take on. And unless or until Fluidity stabilizes and emerges as some sort of <em>de-facto</em> community standard there is no particular justification for incorporating a subset of it into the core Fabric API.

It seems more likely that Fabric API will eventually incorporate some less comprehensive set of interfaces that may be imfluenced by or derived from this and other community efforts, such as LBA (mentioned below.). In that case, the author intends to make Fluidity support and extend relevant "official" APIs as they emerge.

### LibBlockAttributes
Fluidity necessarily covers some of the same ground as [LBA](https://github.com/AlexIIL/LibBlockAttributes).  The author intentionally did not closely study LBA while Fluidity was being developed, both in order avoid making a derivative work and to ensure a fresh perspective.  That said, cross-compatibility is an explict goal and will be pursued when Fludity stablizes and as time permits.  From admittedly superficial observation, this appears attainable with reasonable effort.

### Cardinal Components
Fludity Device Components (explained below) may be seen to overlap somewhat with [Cardinal Components API](https://github.com/NerdHubMC/Cardinal-Components-API). However, this apparent redundancy is superficial.  

Fluidity Device Component Types focus on discovery and retrieval of implementations and does not provide dynamically extensible data attributes, serialization or other facilities offered by CCA.  Indeed, CCA may prove to be quite complimentary to some Fluidity implementations.    

### Vanilla Minecraft
Fluidity currently has no Mixins and makes no (intentional) changes to vanilla Minecraft behaviors.  Implementations are expected to make their own choices regarding their compatibility with vanilla mechanics.  The poor encapsulation of `Inventory` and it's sub-types is particularly problematic for large-scale storage networks that aim to be performant, and there is no clear best answer for how compatible vs how optimized any given block or item ought to be - it depends on how it will be used.

The library offers some base implementations and helpers to more easily support `Inventory` when that is the desired outcome, along with a way to register handlers for buckets, bottles and similar items.  Even so, vanilla mechanics received cursory attention at best in the initial development and the author considers this an area of opportunity for future improvement. 

# Overview

## How Fluidity is Organized
The fludity source tree is divided into four packages as follows:
* [**`grondag.fludity.api`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/api)
Public interfaces for use and implementation by mods.  These are the only types an implementation *must* use or be aware of.

* [**`grondag.fludity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base)
Base implementations and helpers.  These are extensive and probably of interest to most implementations but their use is entirely optional.

* [**`grondag.fludity.impl`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/impl)
Internal implementations and helpers.  Mods should not directly reference anything in this sub-tree.

* [**`grondag.fludity.wip`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/wip)
Work-in-process code that will *probably* become part of the library in the near future but is more experimental than even the API Guarding `EXPERIMENTAL` annotation would indicate.  Mods are welcome to look at it, test and provide feedback but should have no expectation of stability. This sub-tree replicates the api/base/impl divisions of the main API to indicate where the code will eventually land.

## Articles
An `Article` is a game resource that can be uniquely identified, quantified and serialized. An `ArticleType` defines the class of resource and provides packet and NBT serialization functions.  

Fluidity pre-defines two article types: `ArticleType.ITEM` and `ArticleType.FLUID` to represent in-game items and fluids.  However, any class can be used as an article type via `ArticleType.Builder` and `ArticleTypeRegistry`.  Some possible uses would include non-fluid bulk crafting resources, XP, power systems, or mana.  

Note that no restriction is made against defining new article types that also use `Item` and `Fluid` resources.  However, for compatibility it is recommended that mods adopt the predefined `ITEM` and `FLUID` article types for inter-mod resource storage and transport.

### Discrete vs Bulk Articles
The creator of an article type chooses if the article is *discrete* or *bulk*.  Discrete articles are meant to be counted as individual, atomic units.  Bulk articles are divisible into some unit less than one or fully continuous and thus meant to be measured using fractions. (More on those in a bit.)

However, this distinction is *purely advisory.*  Fluidity is designed so that *any* article type can be measured using either sort of accounting.  Whole numbers, after all, are simply a sub-set of the rational numbers.  The main benefit to using integers over fractions is slightly better performance and memory efficiency.  But if you want to build an "item tank" that stores fractional pick-axes, this is the library for you.

### Getting an Article Instance
Use `Article.of(articleType, resource)` to get a custom article.  `Article.of(item)` and `Article.of(fluid)` are more concise for those common types.  `Article` also exposes static methods for de-serializing any article from an NBT tag or packet buffer.

Retrieving an article is generally non-allocating after the first retrieval because all article instances are interned or cached. This means article instances can be compared using `==` *unless* they contain an NBT tag. (See below)  For this reason, Articles should always be compared using `.equals()` unless the situation absolutely ensures no tags are present on any article being compared.

### Article NBT Tags
An `Article` *may* have an NBT CompoundTag value associated with it.  Currently this functionality is only exposed for `ITEM` articles (because Minecraft requires it) but may be opened up for other article types in a future release.  However, mod authors are *strongly* advised to avoid using tags in favor of simply creating a larger number of distinct resource instances.

When an article has a non-null tag value there can be a virtually infinite number of distinct instances. Interning such articles would create the risk of excessive memory allocation.  Thus, article instances with tag values are held in a fixed-capacity cache and evicted as needed, making them slightly less efficient than articles without tags.

To ensure that articles are immutable, an article's tag instance is not directly exposed. The `copyTag()` method is the only way to get the tag content, but results in a new allocation every time.  If you only need to test for tag existence or test for tag equality use `hasTag()` or `doesTagMatch()`.

### Stored Articles
When an article is being stored or transfered we need additional information: quantity and, sometimes, a location. A core design principle of Fluidity is that all such data should never be directly mutated outside of the storage/transport implementation - all changes *must* be the result of some controlled, observable transaction.

This is why the API that exposes this information is immutable: `StoredArticleView`.

`StoredArticleView` includes *both* a whole-number `count()` (for discrete articles) and a fractional `amount()` (for bulk articles).  This creates a tiny amount of extra work for implementations (which is largely handled automatically via the base packages) at the benefit of having fewer/simpler interfaces overall.  Consumers of the API can use whichever accounting method makes sense for their purpose.  

Note that the `count()` property will not reflect fractional amounts less than a unit and so is not a reliable test of emptiness for implementations that may contain bulk items.  To test for emptiness, use `isEmpty()`.

`StoredArticleView` also has a special instance meant to be used in place of `null` values: `StoredArticleView.EMPTY`.  Implementations should return this instance instead of `null` when the intent is to signal the absence of a result.

#### Implementation Support
Obviously, implementations *will* need to mutate their contents and most implementations will be firmly discrete or bulk - not both.  The [`grondag.fluidity.base.article`]() package provides specialized discrete/bulk interfaces and classes to support most types of implementations.  Use of these is entirely optional but mod authors are encouraged to examine them for illustration before creating their own.  

#### Stored Article Handles
`StoredArticleView` also exposes an integer `handle()` property, which is *similar* in purpose to vanilla inventory "slots" but also different in key ways:

* Handles are not guaranteed to correspond to a specific, "physical" location in storage. Some implementations (something like a Storage Drawers mod, for example) may have this contract, but it is never required.

* Handle can be used to retrieve a storage view (similar to `List.get()`) but the targets of storage transactions are *always* specified by article - never by handle.  This ensures that no transaction is ambiguously or erroneously specified. A vanilla `Inventory` will let you blindly replace or change the contents of a slot without knowing or validating what was in it. Fluidity *never* allows this in its public API. Implementations that extend this to allow transactions based on handle (again, something like Storage Drawers would require this) are advised to *also* include article as part of any transaction specification. (The `FixedDiscreteStorage` interface in `grondag.fluidity.base.storage.discrete` and its sub-types offer an example of this.)

* Storage implementations are required to maintain a consistent handle:article mapping for as long as the storage has any listeners. In practice, this means preserving empty handles and creating new ones when articles are completely removed and new articles are added.  This makes is much easier for listeners to maintain a synchronized view of content and/or respond appropriately to changes.  Except...

* Implementations that *do* have physical slots *may* change a handle:article mapping, but when doing so must send listeners two events: one to remove the article from its current handle association (the listener would associate the old handle with `StoredArticleView.EMPTY`) and a second event to re-add the article with its new handle.  Implementations that have other reasons to change handle:article mappings may also do so if they follow the same practice.

## Fractions
Fluidity represents Fractions as three `long` values: whole units, numerator and denominator.  While two longs (numerator and denominator) would arguably be sufficient for most use cases, that arrangement would mean that scale and sub-unit resolution would would vary inversely.  In an unpredictable multi-mod environment, it is better for mod authors (and mod testers) if the maximum resolution and scale of fractions are both invariant.

Fluidity includes two concrete implementations as part of the public API: `Fraction` and `MutableFraction`. These are and do exactly what you would expect based on the their names. Implementations that frequently update fractional values will generally want to use `MutableFraction` instead of allocating new `Fraction` instances with every operation.

For this reason, consumers of `Fraction` should *never* retain a reference but instead copy the value to a `MutableFraction` instance, or use `Fraction.toImmutable()` to ensure you have a Fraction instance that is safe to hold.

These classes were designed to be final in order to ensure static invocation in what are likely to be frequent calls and mod authors should not try to sub-class or modify them via reflection, Mixins or other esoteric methods. If you really need them to do something else, please submit a PR.

### Fractions without 'Fraction'
In many places, fractional quantities are exposed and manipulated as primitive values, without use of `Fraction` itself.  These interfaces (see below and in the code) instead rely on `long` values that correspond to `Fraction.whole()` or `Fraction.numerator()` and `Fraction.denominator()`.  When available and appropriate for your use case (i.e. when you have fixed minimum and maximum transfer amounts) these will generally be more performant and easier to work with. 

## Store and its Variants
A `Store` in Fluidity is an instance that holds and, optionally, supplies or accepts Articles. Stores may also publish an event stream that is useful for synchronizing store contents to client-side GUIs or implementing aggregate views of multiple stores.

A store may contain any `ArticleType` or combination of article types, using either discrete or bulk accounting and the interfaces are designed so that all implementations *must* support queries and operations using either discrete or bulk (fractional) quantities. This allows consumers of any storage implementation to have rigid and simple code paths, and helps to limit redundancy and the size of the API more generally.

In practice, most implementations are likely to store one article type (or a small, focused set of article types) and will use the form of accounting (discrete or fractional) most appropriate for that set.  Fluidity includes base classes and helpers to make this flexibility relatively easy to attain - mod authors should only need to handle the use cases that matter for their implementation.

### Querying Store Content
Unlike `Inventory` a `Store` is never asked to expose its internal representation of content, or even all of its content. You can ask a store a question, and it will give you the answer it wants you to have. A store should not lie, but it doesn't have to tell the whole truth.  Consumers should expect store query results to be consistent, but shouldn't try to infer information the store hasn't explicitly provided.

#### Best Practice: Don't Query Store Contents
The rest of this section will describe the various ways you can get information about a store, most of the time you should not. If a store reports it has an article, that does *not* imply the store will supply a certain amount of the article if asked.  If a store is empty, that does not mean it can accept a particular article in whatever amount.

The *only* reliable way to know if a store can accept or supply some quantity of an article is to try it - simulating if the intent is informational.  This is a very deliberate limitation in the contract of the `Store` interface, and is meant to ensure that `Store` implementations have flexibility in how they operate.  Any attempt to codify the rules what a chest or tank might be allowed to do would have to anticipate every possible use case (and thus be very extensive). And it would almost certainly fail in this attempt, eventually becoming an obstacle to somebody's cool idea.

Another expected use of store queries might be to synchronize information to clients for display, or to aggregate contents from multiple stores into a single view.  In both cases, subscribing to the stores' event streams (described below) will be more reliable and performant.

The best (and intended) use of the query mechanism explained below is to emulate `Inventory` behaviors, or gather summary information for *ad-hoc* display or debugging, especially when working with stores from different mods. They also serve as a back-stop to handle use cases that were not otherwise anticipated. 

#### Querying by Handles
As described earlier, a `StoredArticleView` exposes a `handle()` property, and iterating a store's contents via handle or retrieving the article view for a specific known handle will usually be the most straightforward way to navigate store content in those rare cases when it is necessary. The relevant members are `handleCount()` and `view(handle)`.

Stores are generally not meant to be thread-safe and consumers should check `handleCount()` or `isHandleValid()` before calling `view(handle)` but `Store` implementations should return `StoredArticleView.EMPTY` when an invalid handle is encountered.  This is a functionally correct result, and simplifies store implementation and usage overall.

#### Query via `forEach`
`Store` also exposes a pair of specialized `forEach()` methods.  These methods have naive default implementation that simply iterate using handles, but should be overridden by `Store` implementations where iteration by handle may be expensive. Unlike a standard `forEach` method, these accept a `Predicate` as the action, and iteration will stop if the predicate returns `false.` This behavior is especially useful when you only need to act on the first article. An alternate version of `forEach()` accepts a second predicate the `Store` will use to filter articles before applying the action.  This simplifies the action predicate, and may be faster for some implementations.

#### Querying Quantities
Sometimes, you just need to know if a store has a particular article in stock.  The `countOf()` and `amountOf()` methods offer this ability. But note again: the only reliable way to know if a store can actually supply an article is to request it and observe what happens.  This is exactly how these methods work in their default implementation - they simulate maximum extraction of the requested article and return the result.

### Store Operations
While a store may receive requests for any article type with either discrete or fractional quantities, a store can reject or partially fulfill any request that isn't supported,  simply by returning zero or a lesser amount than what was requested. 

For example, a fluid tank could be implemented using discrete accounting to store only whole buckets.  Such a tank would return empty results for any request to fill or drain any sub-unit amount, and would only partially fulfill requests for one or more units that also include a fractional amount.  A tank could also be designed to only accept a single type of fluid, and thus reject any request to drain or fill other fluids (or other types of articles).  More generally, implementations can adopt any constraint that doesn't violate the contract of exposed interfaces, and those interfaces were designed to allow flexibility.

#### ArticleFunction
Storage input and output operations use the same interface: `ArticleFunction`.  

`ArticleFunction` is overloaded, but every variant accepts an `Article` (or something that can be converted to an `Article`, like an `Item`) and a `simulate` parameter.  When the `simulate` parameter is true, the result of the operation will be a forecast only and the state of the Store will not change.

The remaining variation is in how quantities are specified, as follows:

* A single `long` value - for discrete articles or when only whole (bucket) units are needed.  The result will also be a whole unit.

* Two `long` values - a numerator and denominator - for fractional accounting using primitives.  The result will a  multiple of the denominator.

* A Fraction instance - for full-scale, full-precision fractional accounting. The result will be a Fraction.

All `ArticleFunction` implementations *must* accept all of these variants, but as with `Store` most implementations will adopt a specific form of accounting internally and rely on the base classes provided by Fluidity to convert mismatched requests to an appropriate form.  

Note there are some subtle differences in how the quantity input affect the outputs: variations that accept `long` values must also return results that can be expressed as `long` values.  This means, for example, that a request to supply 2/3 from a tank that is 1/2 full can only return 1/3 if the result must be expressed as two long values. But if the same request is made with 2/3 (as a `Fraction`) then the result will be 1/2 (also as a `Fraction`).

The lesson here is this: if you can accept Fraction values then you should also make requests using Fraction values.  If you can't accept Fraction values, then primitive values are exactly what you need, because that will ensure you never get a result you can't accept.  In either case you should not assume an empty result means a store is empty or full - it only means the store can't supply or accept the amount requested in the form you requested it.   (Use `Store.isEmpty()` or `Store.isFull()` to answer those questions.)

#### Supplier and Consumer Article Functions
The quantity parameters to `ArticleFunction`, in any form, are *always* zero or positive.  (Fraction numerators must be >= 1) The direction of articles in or out of storage is instead implied by which article function is called: 

* **`Store.getSupplier()`** Use to remove articles from a store. (Or to forecast the result of a removal request,)

* **`Store.getConsumer()`** Use to add articles to a store. (Or to forecast the result of an add request.)

Both methods by default return `ArticleFunction.ALWAYS_RETURN_ZERO` - a special implementation of `ArticleFunction` that, unsurprisingly, always returns zero in response to any request. A store can be made insert-only or extract-only by overriding only one of these methods.

Implementations should not override these methods to return `null`. The default return value can be used as-is with the (correct) result that no operation will change state.  Classes that do frequent operations on many stores may see some performance benefit from excluding inoperable stores by testing for the presence of a real implementation using the convenience methods `hasConsumer()` and `hasSupplier()`. 
 
### Store Event Streams and Storage Listeners
A store can optionally expose a `StorageEventStream` (via `storageEventStream()`) to broadcast changes to storage state to interested listeners.  Listeners must implement `StorageListener` and pass themselves to `StorageEventStream.startListening()` to begin receiving events.

The `StorageListener` interface, like others in Fluidity, must handle both discrete and fractional accounting.  To this end, there are two versions of events, one with a single long quantity and one with a fraction.  Here as elsewhere, Fluidity includes base classes to reduce the burden on implementations.

As with `ArticleFunction`, quantity parameters are always positive.  Direction is shown by which method is called: `onAccept()` for articles coming into the store, and `onSupply()` for articles going out.  These methods include both the amount changed and the new amount that results.  This bit of redundancy is easy for the store to provide (it must have this information anyway) and makes many listener implementations simpler if they have no need to track prior state. It also makes it easier to identify state synchronization errors.   

Stores should not notify listeners of simulated requests. It would serve no useful purpose and there is no simulation flag in any of the notification methods.

Stores that are enrolled in a transaction (explained in a later section) should generally send notifications immediately and not wait for the transaction to be committed. Some listeners may depend on the store's state and could also be enrolled in the same transaction.  If such listeners are not notified of events as they happen, they could give invalid results.  This means that if a transaction is rolled back, the store must send events that reverse earlier notifications.  Again, the quantities are always positive - an `onAccpt()` notification is reversed by a matching `onSupply()` notification, etc.      

A store should respond to a new listener by immediately sending acceptance notifications for current content that should be visible to listeners.  This avoids the need for any listener to know how to query the store to build an initial state vector that will be exactly synchronized with the event stream. This behavior can be disabled when unneeded via the `sendNotifications` parameter to `startListening()`.  

The same pattern applies to the `stopListening()` method - the store should respond by sending supply notifications for all current content unless asked not to.  This behavior is particularly useful for aggregate views of multiple stores because store addition and removal can be handled by the same routines that handle accept and supply events.

Stores that do not implement `Store.eventStream()` should rely on the default implementation to return `StorageEventStream.UNSUPPORTED`. Consumers of event streams should check for the presence of a valid instance using `hasEventStream()` before subscribing. Subscribing to `StorageEventStream.UNSUPPORTED` logs a one-time warning as an aid to troubleshooting but no exception is thrown and no event notifications will be received. 
 
### Implementation Variants
[**`grondag.fluidity.base`**](https://github.com/grondag/fluidity/tree/master/src/main/java/grondag/fluidity/base/storage) and its sub-packages include the following interfaces and classes to facilitate various types of `Store` implementations:

* **`AbstractStore`**  Provides shared components for listener notifications.
    * **`AbstractAggregateStore`**
    * **`AbstractLazyRollbackStore`**
        *  **`AbstractDiscreteStore`**
        *  **`SimpleTank`**


## Devices

## Transactions

## Multiblocks

## Carriers

### Best Practices - enlist and support auto enlist

# Using Fluidity

## Dev Environment Setup

## Examples

