Divider 
=======
#####(проект разрабатывается студентами кафедры [СПУ](http://www.kpispu.info/ru/about) [НТУ "ХПИ"](http://www.kpi.kharkov.ua/ru/))

[English version of README](https://github.com/SPC-project/divider/blob/master/README_en.md)


Это построенное на Swing приложение, предназначенное для подготовки данных для конечно-элементного моделирования деформирования и ползучести (начатое как магистерская работа [Павла Лысака](https://github.com/paul-lysak/divider)). Пользователь может построить фигуру и разбить её треугольниками. 

Обратитесь к [doc/changes.md](https://github.com/SPC-project/divider/blob/master/doc/changes.md) чтобы просмотреть историю изменений.

Инструкция по запуску
----------
Для Unix-подобных систем, с установленным [Maven](https://maven.apache.org/), выполнить в терминале:

	cd <path-to-project>
	mvn package
	mvn exec:java

Либо, можно сгенерировать проект под [Eclipse](https://eclipse.org/) командой:
	
	mvn eclipse:eclipse

Либо, в самом Eclipse, с установленным [m2e](http://www.eclipse.org/m2e/) плагином:

	File > Import > Maven > Existing Maven Project > *выбрать папку с Divider'ом*
