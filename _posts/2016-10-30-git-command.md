---
layout:post
title:"git command"
description:""
category:[git]
tags:[git]
---
{% include JB/setup %}

### create version stock
    
    $ git clone <url>
    $ git init

### update and commit

    $ git status
    $ git diff
    $ git add .
    $ git add <file> 
    $ git mv <old> <new>
    $ git rm <file>
    $ git rm --cached <file>
    $ git commit -m "commit message"

### look for commit history

    $ git log
    $ git log -p <file>
    $ git blame <file>

### reset

    $ git reset --hard HEAD
    $ git checkout HEAD <file>
    $ git revert <commit>

### branch and tags

    $ git branch
    $ git checkout <branch/tag>
    $ git branch <new-branch>
    $ git branch -d <branch>
    $ git tag
    $ git tag -d <tagname>

### merge

    $ git merge <branch>

### remote operate

    $ git remote -v
    $ git remote show <remote>
    $ git remote add <remote> <url>
    $ git pull <remote> <branch>
    $ git push <remote> <branch>
    $ git push --tags

