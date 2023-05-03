# -*- coding: utf-8 -*-
# @Time : 2023/5/2 20:04
# @Author : zhao
# @Email : liming7887@qq.com
# @File : use.py
# @Project : main2.py
import sys

import cv2
import numpy as np
from keras import models


def fun(model_path, data_path, class_path_str):
    """
    :param class_path_str: 计算时需要指定的类别列表
    :param model_path: 计算时需要使用的模型目录
    :param data_path:  需要被计算的数据路径
    :return: 类别编码
    """
    print(f"data_path  = {data_path}")
    print(f"model_data = {model_path}")
    print(f"class_path = {class_path_str}")
    # 获取到list
    file = open(class_path_str, encoding='GBK')
    class_list = file.readlines()
    file.close()
    # 获取到我们用于测试的图像数组
    image = cv2.imread(data_path, cv2.IMREAD_GRAYSCALE)
    image = cv2.resize(image, (87, 87))
    # 使用模型计算结果
    model = models.load_model(model_path)
    res = model.predict(np.expand_dims(image.data, axis=2).reshape([-1, 87, 87, 1]))
    print(res)
    # 找到结果向量中最大得分的索引
    index1, index2, v = 0, 0, res[0][0]
    for n in np.nditer(res[0]):
        if n > v:
            index2 = index1
            v = n
        index1 += 1
    # 使用索引获取到结果类别
    print(class_list)
    return class_list[index2]


if __name__ == '__main__':
    print(f"当前被识别的图像为 ===> {fun(sys.argv[1], sys.argv[2], sys.argv[3])}")