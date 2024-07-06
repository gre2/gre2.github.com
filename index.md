---
layout: page
title: A方案搞不定，可以换成B方案啊。
tagline: 程序是理科不是文科，需要的是理解不是背书。
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
