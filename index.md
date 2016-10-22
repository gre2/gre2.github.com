---
layout: page
title: Hello World!
tagline: Coding on shoulders of Giants with music.
---
{% include JB/setup %}

## Info

my wechat is `gre2`

my email is `gre2` at `163.com`

pay a visit to my [github](http://github.com/gre2).

## Posts

<ul class="posts">
  {% for post in site.posts %}
    <li><span>{{ post.date | date: "%Y-%m-%d" }}</span> ==&gt; <a href="{{ BASE_PATH }}{{ post.url }}">{{ post.title }}</a></li>
  {% endfor %}
</ul>
