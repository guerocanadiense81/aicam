import numpy as np

def _make_grid(nx, ny):
    yv, xv = np.meshgrid(np.arange(ny), np.arange(nx))
    return np.stack((xv, yv), 2).reshape((1, 1, ny, nx, 2))

ANCHORS = (
    np.array([[10,13],[16,30],[33,23]]),
    np.array([[30,61],[62,45],[59,119]]),
    np.array([[116,90],[156,198],[373,326]])
)

sigmoid = lambda x: 1.0/(1.0+np.exp(-x))

def nms(boxes, scores, iou_thres=0.45):
    x1, y1, x2, y2 = boxes.T
    areas = (x2-x1+1)*(y2-y1+1)
    order = scores.argsort()[::-1]
    keep = []
    while order.size>0:
        i = order[0]; keep.append(i)
        xx1 = np.maximum(x1[i], x1[order[1:]])
        yy1 = np.maximum(y1[i], y1[order[1:]])
        xx2 = np.minimum(x2[i], x2[order[1:]])
        yy2 = np.minimum(y2[i], y2[order[1:]])
        w = np.maximum(0.0, xx2-xx1+1); h = np.maximum(0.0, yy2-y1+1)
        inter = w*h
        ovr = inter / (areas[i] + areas[order[1:]] - inter)
        inds = np.where(ovr <= iou_thres)[0]
        order = order[inds+1]
    return keep

def postprocess_yolov5(outputs, img_size=320, conf_thres=0.25, iou_thres=0.45, num_classes=80, person_cls=0):
    candidates = []
    strides = [8,16,32]
    for i, out in enumerate(outputs):
        o = out
        if o.ndim == 4:
            na = 3
            o = o.reshape((1, na, o.shape[2], o.shape[3], o.shape[4]))
        na = o.shape[1]; ny, nx = o.shape[2], o.shape[3]
        o = o.copy()
        o[...,0:5] = sigmoid(o[...,0:5])
        o[...,5:]  = sigmoid(o[...,5:])
        grid = _make_grid(nx, ny)
        anchor = ANCHORS[i].reshape((1, na, 1, 1, 2))
        stride = strides[i]
        xy = (o[...,0:2]*2.-0.5 + grid) * stride
        wh = (o[...,2:4]*2.)**2 * anchor
        x1y1 = xy - wh/2; x2y2 = xy + wh/2
        obj = o[...,4:5]; cls = o[...,5:]
        conf = obj * cls[...,person_cls:person_cls+1]
        mask = conf[...,0] > conf_thres
        boxes = np.concatenate([x1y1, x2y2], -1)[mask]
        scores = conf[...,0][mask]
        for b,s in zip(boxes, scores):
            candidates.append(np.array([b[0],b[1],b[2],b[3],s,0.0]))
    if not candidates:
        return np.zeros((0,6), dtype=np.float32)
    dets = np.stack(candidates).astype(np.float32)
    keep = nms(dets[:, :4], dets[:, 4], iou_thres)
    return dets[keep]
