---
layout: post
title: "create my github page"
description: ""
category: [jekyll, github]
tags: [jekyll, github]
---
{% include JB/setup %}


### steps on windows

1. create repositry on github

    1. create a new repositry named [gre2.github.io]

    1. where `gre2` is my github username

1. install Jekyll
  
    1.preparatory work

        install ruby
        install rubygems

    2.use rubygems to install jekyll

        gem install jekyll


2. create a post

    1. do some edit

    1. serve it

            $ jekyll serve

    1. browser it

            http://localhost:4000

    1. commit and push

            $ git add .
            $ git commit -m "create my github page"
            $ git push origin master

### setup jekyll on windows

1. [reference](http://jekyll.com.cn/)
