db = db.getSiblingDB("cowork_group");
db.group.save({
    "_id": ObjectId("602923a34900db17f08617ab"),
    "name": "研发部",
    "describe": "大家好，这里是研发部！",
    "leader": "60292360786fa61295fd82a5",
    "users": [
        "60292360786fa61295fd82a5",
        "60672654a942ee7c5f2041c7"
    ],
    "docs": [
        "602924fb64fadf4bad2e1a37"
    ],
    "isEnable": true,
    "memRole": "Create",
    "createTime": ISODate("1970-01-01T00:00:00.00Z"),
    "updateTime": ISODate("1970-01-01T00:00:00.00Z"),
    "_class": "com.shiroyk.cowork.coworkcommon.model.group.Group"
});