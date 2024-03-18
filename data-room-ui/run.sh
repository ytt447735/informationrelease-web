#!/bin/bash

# 进入dist目录
cd "$(dirname "$0")/dataRoomUi"

# 打包dist文件夹到zip压缩包
zip -r ../dataRoomUi.zip .

# 返回到脚本根目录
cd ..