package event

// Event of collab
type Event uint8

const (
	_ Event = iota
	Login
	Logout
	Sync
	Update
	Save
	Cursor
)

const (
	StreamPersistence = "stream_collab_persistence"
	HeaderSource      = "X-Source"
	subjectPrefix     = "events."
)

var eventSubjects = [...]string{
	Login:  subjectPrefix + "login",
	Logout: subjectPrefix + "logout",
	Sync:   subjectPrefix + "sync",
	Update: subjectPrefix + "update",
	Save:   subjectPrefix + "save",
	Cursor: subjectPrefix + "cursor",
}

func (e Event) String() string { return eventSubjects[e] }

func (e Event) Subject() string { return eventSubjects[e] }

type CollabMessage struct {
	Event Event  `msgpack:"event"`
	Uid   string `msgpack:"uid"`
	Did   string `msgpack:"did"`
	Data  []byte `msgpack:"data"`
}
