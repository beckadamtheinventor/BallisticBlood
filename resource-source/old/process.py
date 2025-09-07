#!/usr/bin/python3
from PIL import Image

def rot(x, count):
    return x*(360/count) + (360/count)*(9/8)*(x&3)

def f(n, o, count):
     i = Image.open(n)
     i2 = Image.new(i.mode, (i.width*count, i.height))
     for x in range(count):
             i2.paste(i.rotate(rot(x, count)), (x*i.width, 0))
     i2.save(o)

for name in ["ash", "dust", "blood", "slime"]:
    for kind in ["particle", "decal"]:
        n = name+"_"+kind+".png"
        f("src/"+n, n, 16)

