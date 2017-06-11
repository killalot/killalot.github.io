---
layout: post
title:  "Code Coverage - a metric not a goal"
date: 2017-06-11T21:31:49+01:00
keywords: testing, code coverage
---

In this post I want to talk about how to use code coverage to get the most out of it. Also, and perhaps more important, what code coverage can't help you with and how it can be abused.

## So What is Code Coverage?

I know, I know, anybody who reads this should understand what code coverage is but I thought I would start off by providing a definition.

Code Coverage (also known as Test Coverage) is the amount of production code that is executed during testing. Usually when developers talk about code coverage this is the code executed in unit tests. Code coverage doesn't have to be measured just from unit tests though, it could come from integration tests, functional tests or even manual tests. Admittedly its harder to measure coverage from manually testing but this is still running against production code like any other type of testing so in theory it could contribute to the project's Code Coverage metric.

Having said that in this article I will be talking about code coverage obtained through tests written as code of which the majority is typically unit tests.

## An Easy Metric

I suspect one of the main reasons why Code Coverage has spread across developers and managers alike is because it's an easy metric. A manager asks "what is our code coverage currently?" and one of the developers responds: "one sec... ah its 87%". The manager says "that's 2% up from last month, great thanks" and the manager goes about his business.

I characterize Code Coverage as an easy metric for three reasons:

1. It is easy to obtain

   Minimal effort is needed by developers to get this percentage nowadays. There are plenty of libraries out there that provide a means to obtain it, like JaCoCo for Java for instance. Combine this with plugins for IDEs and code quality tools and it almost measures itself.

1. It is easy to understand

   Being a definition that can be contained within a single sentence, you don't need to do much reading to understand it. There isn't anything technical in this definition either, if you know what code is and what tests are then you can follow along. This is particularly important as non technical managers can get on board without needing to learn any new terminology. However understanding what Code Coverage is is one thing, knowing how to use it correctly is a different matter which I'll get into later.

1. It is easy to report

    Being only a single percentage, Code Coverage can easily be passed around between developers and managers alike. Managers can even get the figure themselves if we developers are dumb enough to put it on a dashboard for all to see.

## Does High Code Coverage Equal Quality

I think the definition makes it quite clear, Code Coverage itself makes no judgment on the quality of the tests or code. If you think it does then I would argue that you are mixing in other methodology, much like I'd say what happens with TDD (not that I'm against TDD per say).

But does making sure you have high test coverage *indirectly* produce software that has attributes that could be associated with high quality. Well with high coverage I can assume you will have a high number of tests. Tests are needed in order to execute the different paths through your code base and so if your project is anything more than trivial you will need lots of tests to get high coverage. So can we say a high number of tests is a measure of quality, I would say no. Sure a high number of tests is better than some tests which is better than none, but only if we assume they have all been written with the same amount of care. If the tests are not written properly then you could be worse off having high coverage due to the false sense of security and maintenance headache. If you have a good team working on the project from day one then maybe code coverage can be significant to quality but this becomes largely irrelevant anyway as a good team will have high coverage as a byproduct of the practices they follow.

Overall Code Coverage does present itself as easy out but unfortunately quality of software is very hard to measure. It requires a good grasp of software principles and years of experience before you start to understand or even can discuss the components that make up a quality product. As an isolated metric Code Coverage only goes skin deep at best.

## Code Coverage Induced Damage

If you combine its ease of use and thinking that overall code coverage says more about quality than it actually does, you have a recipe for code coverage induced damage.

This is something I have witnessed myself. I had put code coverage on a code quality dashboard thinking that it was a valuable thing to know and whether it was going in the right direction. Though my intentions were good I started noticing things that worried me. Putting it on the board naturally made developers see it as something to improve during slack time and so we had pull requests coming in directly addressing code coverage. Although it was good that more tests were being added, they were being made with the wrong mindset - increasing code coverage. This gave the scope for overlooking attributes that make up a well written test. Now I'm not saying that badly written tests will always result from having code coverage as a goal, but more care does have to taken and you can be more easily lead astray.

Code coverage used in this way is working against you, tempting you to go for the quicker option to get the percentage you're aiming for.

## Using Code Coverage Effectively

From what I've said you might think I want to abandon code coverage all together! Well I do think we should stop using code coverage as an overall metric since, at least in my experience, it causes more trouble than its worth. Rather than using Code Coverage for the project as a whole, we can apply it specifically to the piece code being worked on. I see it being useful in 3 ways:

- To detect potentially missed scenarios

    When writing tests for new or existing code, we have an expectation of the amount of coverage this will give and where in the code base this is located. If you are following TDD for example then you would expect near 100% coverage. You can use Code Coverage as a way to check if your tests are missing any scenarios/paths through the code. Certainly though this isn't the be all and end all way of checking for untested scenarios as there can be multiple scenarios being dealt with by the same piece of code so Code coverage cannot detect these.

- Identifying superfluous code

    If you are removing tests and code that are no longer needed (the business needs have changed for example), you can use Code Coverage to highlight any code that was missed during the removal. This is also useful during refactoring - after making some alterations you can check if any code has now been made redundant. When you are writing tests for legacy code, again you can identify potentially redundant code or at very least promotes you to investigate further into what the intension of that code is.

- Aid understanding of legacy code

    Unfortunately sometimes all you have to work with is a big ball of mud with no tests to guide the way. You can start to get to grips with this by writing a test with some input values and see what happens. Looking at the Code Coverage of this test you can see what parts of the code are being called given these inputs. This should allow you to narrow your focus and begin to clean up that ball of mud.

## Summary

Code Coverage is still a valuable metric providing it is not used as something to directly aim for but rather a tool to help you write better code. Brian Marick I think sums it up nicely:

> I wouldn’t have written four coverage tools if I didn’t think they’re helpful. But they’re only helpful if they’re used to enhance thought, not replace it.

# References and Further Reading

- [SwanseaCon 2015: Sandro Mancuso](https://youtu.be/aCLBd3a1rwk)
- [TestCoverage](https://martinfowler.com/bliki/TestCoverage.html) by Martin Fowler
- [TDD is dead conversation part IV](https://youtu.be/dGtasFJnUxI)
- [How to Misuse Code Coverage](http://www.exampler.com/testing-com/writings/coverage.pdf) by Brian Marick
