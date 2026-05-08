* so first vid is reqeusted with the vid ID;
* it is a get reqeust so it will be qeurytized url : vidService will do the vid proccesing and handling
* if it is the first time then a wathc history will be opened will be handled by watchService
* then the vid service will return the segement number and the vid location.


* when it is the get vid reqeust instead of asking for locaton the browser has to send the token in the header plus the vid ID , plus the segemnt num it is reqeusting
* the whole thing will be cached
* now for the first time it will ask thed db to give it the id of watch that is assoiated with that Vid id and user ID and  save it
to the caffeine cache

the saved info will be VId id plus latest segemnt and then will be flushed

get vid:
B >>>>>(VID)>>>>> WatchVidCotroler (VID, UUID) >>>>>>>>VidService(does Chking)>>>>>WatchService( Create history if new)>>>>Ret Segment NUm
>>>>>SegNUm + Vid LOCation >>>>> B

get seg:
B >>>> Header (token, VidID, SegNum)>>>>WatchVidControler >>>>(UseDetailes, VidID, SegNUM)>>>Catchble req to DB to fetch WatchID>> if it doen exist thorw an error>>>> Save it as a cache to <WatchID, SegNUM>