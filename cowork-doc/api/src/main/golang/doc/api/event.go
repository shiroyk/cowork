package api

// Event of collab
type Event int

const (
	_ Event = iota
	LoginEvent
	LogoutEvent
	SyncEvent
	UpdateEvent
	SaveEvent
)

const (
	StreamPersistence = "stream_collab_persistence"
	HeaderSource      = "X-Source"
	subjectPrefix     = "events."
)

var eventSubjects = [...]string{
	LoginEvent:  subjectPrefix + "login",
	LogoutEvent: subjectPrefix + "logout",
	SyncEvent:   subjectPrefix + "sync",
	UpdateEvent: subjectPrefix + "update",
	SaveEvent:   subjectPrefix + "save",
}

func (e Event) String() string { return eventSubjects[e] }

func (e Event) Subject() string { return eventSubjects[e] }

type CollabMessage struct {
	Event Event  `msgpack:"event"`
	Uid   string `msgpack:"uid"`
	Did   string `msgpack:"did"`
	Data  []byte `msgpack:"data"`
}
