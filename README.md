# AddonDowner
Small client to help you keep add ons up to date in world of warcraft

I made this client cause there is no curse client for Mac. Orignial I only made a small perl script that used external shell utilities to do this. But I have lately even starting playing on a windows and used Curse client but I really miss my perl script so I tried to make it run on windows but it was to much work with so I made this small java app (probobly more work but much more fun) that I can use on both my machines. It's only I small app to basicly automates manually downloads. 

It free for anyone to use but I don't garantee anything, it's very dependet on the sites that it downloads addons from so if anything change it will be useless. It can handle most addons from curse and you add them like "http://www.curse.com/addons/wow/deadly-boss-mods/download" (link from download button of the site). It even download Elvui from "http://www.tukui.org/dl.php".

Normally it should be a runnable version in artifact subdir, if your on windows you can use the bat file to start the app with. You need to have atleast Java 7 installed before trying the app.

Enjoy
/Johan

### Versions
+   0.2 alpha - added simpler adder, has list on server that is update when people is adding there own addon, refactor
+   0.1 alpha - first version with basic functions, simple add only from url, working download and refresh of addon, checking when addons should be downloaded, autoupdate on start, auto quit after update