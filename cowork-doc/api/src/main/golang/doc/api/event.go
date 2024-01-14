package api

// Event of collab
type Event uint8

const (
	_ Event = iota
	EventLogin
	EventLogout
	EventSync
	EventUpdate
	EventSave
)

const (
	StreamPersistence = "stream_collab_persistence"
	HeaderSource      = "X-Source"
	subjectPrefix     = "events."
)

var eventSubjects = [...]string{
	EventLogin:  subjectPrefix + "login",
	EventLogout: subjectPrefix + "logout",
	EventSync:   subjectPrefix + "sync",
	EventUpdate: subjectPrefix + "update",
	EventSave:   subjectPrefix + "save",
}

func (e Event) String() string { return eventSubjects[e] }

func (e Event) Subject() string { return eventSubjects[e] }

type CollabMessage struct {
	Event Event  `msgpack:"event"`
	Uid   string `msgpack:"uid"`
	Did   string `msgpack:"did"`
	Data  []byte `msgpack:"data"`
}
