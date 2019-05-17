---
layout: page
title: 为了什么？
tagline: 为了家人！
---
{% include JB/setup %}

## Info

my email is `15101179616` at `163.com`

my [github](http://github.com/gre2).

## Posts

<ul class="posts">
  {% for post in site.posts %}
    <li><span>{{ post.date | date: "%Y-%m-%d" }}</span> ==&gt; <a href="{{ BASE_PATH }}{{ post.url }}">{{ post.title }}</a></li>
  {% endfor %}
</ul>
