package DTOs

type Regist_node struct {
	Ip_addr     string `json:"ip_addr"`
	Port_number string `json:"port_number"`
}

type SubmitCurrenFrameDTO struct {
	WatchId      string  `json:"watchId"`
	CurrentFrame float64 `json:"currentFrame"`
}
